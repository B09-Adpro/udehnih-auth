package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.model.User;
import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;
    private User user;
    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long jwtExpiration = 86400000;
    private final long refreshExpiration = 604800000;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", refreshExpiration);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@example.com")
                .password("password")
                .authorities(authorities)
                .build();

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPassword("password");
        user.setRegistrationDate(LocalDateTime.now());
        user.setRoles(new HashSet<>());
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com");
        String token = buildTestToken(claims, "1", jwtExpiration);

        String email = jwtService.extractUsername(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void extractUserId_ShouldReturnCorrectId() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com");
        String token = buildTestToken(claims, "1", jwtExpiration);

        Long userId = jwtService.extractUserId(token);

        assertEquals(1L, userId);
    }

    @Test
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com");
        String token = buildTestToken(claims, "1", jwtExpiration);

        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
        verify(tokenBlacklistService).isBlacklisted(anyString());
    }

    @Test
    void isTokenValid_WithDifferentEmail_ShouldReturnFalse() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "different@example.com");
        String token = buildTestToken(claims, "1", jwtExpiration);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_WithBlacklistedToken_ShouldReturnFalse() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com");
        String token = buildTestToken(claims, "1", jwtExpiration);

        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(true);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertFalse(isValid);
        verify(tokenBlacklistService).isBlacklisted(anyString());
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com");
        String token = buildTestToken(claims, "1", -10000);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertFalse(isValid);
    }

    @Test
    void extractClaim_ShouldExtractSpecificClaim() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("customClaim", "customValue");
        claims.put("email", "test@example.com");
        String token = buildTestToken(claims, "1", jwtExpiration);

        String customClaim = jwtService.extractClaim(token, c -> c.get("customClaim", String.class));
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        assertEquals("customValue", customClaim);
        assertEquals("1", subject);
        assertNotNull(issuedAt);
        assertNotNull(expiration);
    }

    @Test
    void extractExpiration_ShouldReturnCorrectDate() {
        long currentTime = System.currentTimeMillis();
        Date expDate = new Date(currentTime + 5000);
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com");

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject("1")
                .setIssuedAt(new Date(currentTime))
                .setExpiration(expDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        Date extractedExpiration = jwtService.extractExpiration(token);

        assertNotNull(extractedExpiration);
        assertTrue(Math.abs(expDate.getTime() - extractedExpiration.getTime()) < 1000);
    }

    @Test
    void generateTokenWithExtraClaims_ShouldCallGenerateTokenWithUserIdAndClaims() {
        // Create a JwtService spy that we can partially mock
        JwtService spy = spy(jwtService);
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");

        // Set up the repository mock
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Mock the internal method to avoid actual JWT generation
        doReturn("mocked-token").when(spy).generateToken(eq(1L), eq("test@example.com"), eq(userDetails));

        // Call the method we want to test
        String token = spy.generateToken(extraClaims, userDetails);

        // Verify the expected behavior
        assertEquals("mocked-token", token);
        verify(userRepository).findByEmail("test@example.com");
        verify(spy).generateToken(eq(1L), eq("test@example.com"), eq(userDetails));
    }

    @Test
    void generateRefreshToken_ShouldCallGenerateRefreshTokenWithUserId() {
        // Create a JwtService spy that we can partially mock
        JwtService spy = spy(jwtService);

        // Set up the repository mock
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Mock the internal method to avoid actual JWT generation
        doReturn("mocked-refresh-token").when(spy).generateRefreshToken(eq(1L), eq("test@example.com"), eq(userDetails));

        // Call the method we want to test
        String token = spy.generateRefreshToken(userDetails);

        // Verify the expected behavior
        assertEquals("mocked-refresh-token", token);
        verify(userRepository).findByEmail("test@example.com");
        verify(spy).generateRefreshToken(eq(1L), eq("test@example.com"), eq(userDetails));
    }

    private String buildTestToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}