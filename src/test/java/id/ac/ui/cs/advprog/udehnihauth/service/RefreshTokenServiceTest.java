package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.exception.TokenRefreshException;
import id.ac.ui.cs.advprog.udehnihauth.model.RefreshToken;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import id.ac.ui.cs.advprog.udehnihauth.repository.RefreshTokenRepository;
import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User user;
    private RefreshToken refreshToken;
    private final long refreshTokenDurationMs = 604800000; // 7 days

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", refreshTokenDurationMs);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
    }

    @Test
    void createRefreshToken_ShouldDeleteOldTokensAndCreateNew() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        RefreshToken result = refreshTokenService.createRefreshToken(1L);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertFalse(result.isExpired());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));

        verify(userRepository).findById(1L);
        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_WithInvalidUserId_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            refreshTokenService.createRefreshToken(999L);
        });

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(999L);
        verify(refreshTokenRepository, never()).deleteByUser(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void findByToken_ShouldReturnToken() {
        String tokenValue = "valid-token";
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken(tokenValue);

        assertTrue(result.isPresent());
        assertEquals(refreshToken, result.get());
        verify(refreshTokenRepository).findByToken(tokenValue);
    }

    @Test
    void verifyExpiration_WithValidToken_ShouldReturnToken() {
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));

        RefreshToken result = refreshTokenService.verifyExpiration(refreshToken);

        assertEquals(refreshToken, result);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyExpiration_WithExpiredToken_ShouldDeleteAndThrowException() {
        refreshToken.setExpiryDate(Instant.now().minusSeconds(3600));

        Exception exception = assertThrows(TokenRefreshException.class, () -> {
            refreshTokenService.verifyExpiration(refreshToken);
        });

        assertTrue(exception.getMessage().contains("Refresh token was expired"));
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void deleteByUserId_ShouldDeleteTokens() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        refreshTokenService.deleteByUserId(1L);

        verify(userRepository).findById(1L);
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void deleteToken_ShouldInvokeRepository() {
        refreshTokenService.deleteToken(refreshToken);

        verify(refreshTokenRepository).delete(refreshToken);
    }
}