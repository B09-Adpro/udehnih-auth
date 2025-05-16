package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.model.User;
import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        log.info("JWT Secret Key: {}", secretKey != null ? "AVAILABLE" : "NULL");
        log.info("JWT Expiration: {}", jwtExpiration);
        log.info("JWT Refresh Expiration: {}", refreshExpiration);
    }

    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public Long extractUserId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            log.error("Error parsing user ID from token: {}", e.getMessage());
            return null;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return generateToken(user.getId(), user.getEmail(), userDetails);
    }

    public String generateToken(Long userId, String email, UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);

        String[] authorities = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toArray(String[]::new);
        claims.put("authorities", authorities);

        return buildToken(claims, String.valueOf(userId), jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return generateRefreshToken(user.getId(), user.getEmail(), userDetails);
    }

    public String generateRefreshToken(Long userId, String email, UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        return buildToken(claims, String.valueOf(userId), refreshExpiration);
    }

    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final Long tokenUserId = extractUserId(token);
            if (tokenUserId == null) {
                log.warn("Token has no valid user ID");
                return false;
            }

            final String tokenEmail = extractUsername(token);
            if (tokenEmail == null || !tokenEmail.equals(userDetails.getUsername())) {
                log.warn("Token email does not match user details");
                return false;
            }

            if (isTokenExpired(token)) {
                log.warn("Token is expired");
                return false;
            }

            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("Token is blacklisted");
                return false;
            }

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}