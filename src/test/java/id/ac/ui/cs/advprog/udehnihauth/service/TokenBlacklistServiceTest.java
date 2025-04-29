package id.ac.ui.cs.advprog.udehnihauth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistServiceImpl();
    }

    @Test
    void testAddToBlacklist() {
        String token = "test.jwt.token";
        Date expiry = new Date(System.currentTimeMillis() + 3600000);

        tokenBlacklistService.addToBlacklist(token, expiry);

        assertTrue(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    void testIsBlacklistedWithNonBlacklistedToken() {
        String token = "non.blacklisted.token";

        assertFalse(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    void testCleanupExpiredTokens() {
        String expiredToken = "expired.jwt.token";
        Date pastExpiry = new Date(System.currentTimeMillis() - 1000);

        tokenBlacklistService.addToBlacklist(expiredToken, pastExpiry);
        tokenBlacklistService.cleanupExpiredTokens();

        assertFalse(tokenBlacklistService.isBlacklisted(expiredToken));
    }
}