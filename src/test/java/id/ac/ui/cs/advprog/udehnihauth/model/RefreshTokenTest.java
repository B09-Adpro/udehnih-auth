package id.ac.ui.cs.advprog.udehnihauth.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenTest {

    private RefreshToken refreshToken;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(600));
    }

    @Test
    void testRefreshTokenProperties() {
        assertEquals(1L, refreshToken.getId());
        assertEquals(user, refreshToken.getUser());
        assertNotNull(refreshToken.getToken());
        assertNotNull(refreshToken.getExpiryDate());
    }

    @Test
    void testRefreshTokenExpiration() {
        refreshToken.setExpiryDate(Instant.now().minusSeconds(60));
        assertTrue(refreshToken.isExpired());

        refreshToken.setExpiryDate(Instant.now().plusSeconds(60));
        assertFalse(refreshToken.isExpired());
    }
}