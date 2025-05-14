package id.ac.ui.cs.advprog.udehnihauth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;
    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long jwtExpiration = 86400000;
    private final long refreshExpiration = 604800000;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", refreshExpiration);

        userDetails = new User("test@example.com", "password", new ArrayList<>());
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("test@example.com", username);
    }

    @Test
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        String token = jwtService.generateToken(userDetails);
        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
        verify(tokenBlacklistService).isBlacklisted(anyString());
    }

    @Test
    void isTokenValid_WithInvalidUser_ShouldReturnFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails differentUser = new User("other@example.com", "password", new ArrayList<>());

        boolean isValid = jwtService.isTokenValid(token, differentUser);

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithBlacklistedToken_ShouldReturnFalse() {
        String token = jwtService.generateToken(userDetails);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertFalse(isValid);
        verify(tokenBlacklistService).isBlacklisted(token);
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() {
        String token = createExpiredToken();

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertFalse(isValid);
    }

    @Test
    void generateTokenWithExtraClaims_ShouldCreateTokenWithClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("userId", "123");

        String token = jwtService.generateToken(extraClaims, userDetails);

        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractUsername(token));

        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        String userId = jwtService.extractClaim(token, claims -> claims.get("userId", String.class));
        assertEquals("ADMIN", role);
        assertEquals("123", userId);
    }

    @Test
    void generateRefreshToken_ShouldCreateLongerLivedToken() {
        String token = jwtService.generateRefreshToken(userDetails);

        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractUsername(token));

        Date regularExpiration = jwtService.extractExpiration(jwtService.generateToken(userDetails));
        Date refreshExpiration = jwtService.extractExpiration(token);
        assertTrue(refreshExpiration.after(regularExpiration));
    }

    @Test
    void extractClaim_ShouldExtractSpecificClaim() {
        String token = jwtService.generateToken(userDetails);

        String subject = jwtService.extractClaim(token, Claims::getSubject);
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        assertEquals("test@example.com", subject);
        assertNotNull(issuedAt);
        assertNotNull(expiration);
    }

    @Test
    void extractExpiration_ShouldReturnCorrectDate() {
        String token = jwtService.generateToken(userDetails);

        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);

        long expectedTime = System.currentTimeMillis() + jwtExpiration;
        long actualTime = expiration.getTime();

        assertTrue(Math.abs(expectedTime - actualTime) < 1000);
    }

    private String createExpiredToken() {
        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}