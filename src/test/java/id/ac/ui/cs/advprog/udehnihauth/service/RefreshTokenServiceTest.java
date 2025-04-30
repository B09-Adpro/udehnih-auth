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

        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 600000L);
    }

    @Test
    void testCreateRefreshToken() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        RefreshToken createdToken = refreshTokenService.createRefreshToken(user.getId());

        assertNotNull(createdToken);
        assertEquals(user, createdToken.getUser());
        assertFalse(createdToken.isExpired());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void testVerifyExpiration_NotExpired() {
        RefreshToken verifiedToken = refreshTokenService.verifyExpiration(refreshToken);

        assertEquals(refreshToken, verifiedToken);
    }

    @Test
    void testVerifyExpiration_Expired() {
        refreshToken.setExpiryDate(Instant.now().minusSeconds(60));

        assertThrows(TokenRefreshException.class, () -> {
            refreshTokenService.verifyExpiration(refreshToken);
        });

        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void testFindByToken_TokenExists() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> found = refreshTokenService.findByToken(refreshToken.getToken());

        assertTrue(found.isPresent());
        assertEquals(refreshToken.getToken(), found.get().getToken());
    }

    @Test
    void testFindByToken_TokenDoesNotExist() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        Optional<RefreshToken> found = refreshTokenService.findByToken("non-existent-token");

        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteByUserId() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        refreshTokenService.deleteByUserId(user.getId());

        verify(refreshTokenRepository).deleteByUser(user);
    }
}