package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.RegisterRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.TokenRefreshRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.AuthResponse;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.TokenRefreshResponse;
import id.ac.ui.cs.advprog.udehnihauth.exception.TokenRefreshException;
import id.ac.ui.cs.advprog.udehnihauth.model.RefreshToken;
import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import id.ac.ui.cs.advprog.udehnihauth.repository.RoleRepository;
import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private Role studentRole;
    private String jwtToken;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .name("Test User")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        studentRole = new Role(RoleType.STUDENT);
        studentRole.setId(1L);
        studentRole.setUsers(new HashSet<>());

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .password("encodedPassword")
                .registrationDate(LocalDateTime.now())
                .roles(new HashSet<>())
                .build();

        jwtToken = "jwt.token.here";

        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUser(user);
        refreshToken.setToken("refresh-token");
        refreshToken.setExpiryDate(Instant.now().plusSeconds(600));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email is already in use!", exception.getMessage());
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithNewUser_ShouldCreateUserAndReturnToken() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(any(RoleType.class))).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(1L)
                .email(registerRequest.getEmail())
                .name(registerRequest.getName())
                .password("encodedPassword")
                .registrationDate(LocalDateTime.now())
                .roles(new HashSet<>())
                .build();
        savedUser.addRole(studentRole);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doReturn(jwtToken).when(jwtService).generateToken(any(), any(), any());
        when(refreshTokenService.createRefreshToken(anyLong())).thenReturn(refreshToken);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(registerRequest.getEmail(), response.getEmail());
        assertEquals(registerRequest.getName(), response.getName());
        assertTrue(response.getRoles().contains(RoleType.STUDENT));

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(roleRepository).findByName(RoleType.STUDENT);
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(), any(), any());
        verify(refreshTokenService).createRefreshToken(anyLong());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(userRepository.findByEmailForAuthentication(anyString())).thenReturn(Optional.of(user));

        doReturn(jwtToken).when(jwtService).generateToken(any(), any(), any());
        when(refreshTokenService.createRefreshToken(anyLong())).thenReturn(refreshToken);

        user.addRole(studentRole);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals(jwtToken, response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getName(), response.getName());
        assertTrue(response.getRoles().contains(RoleType.STUDENT));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmailForAuthentication(loginRequest.getEmail());
        verify(jwtService).generateToken(any(), any(), any());
        verify(refreshTokenService).createRefreshToken(anyLong());
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAccessToken() {
        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken("refresh-token")
                .build();

        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(any(RefreshToken.class))).thenReturn(refreshToken);

        when(userRepository.findByIdWithRoles(anyLong())).thenReturn(Optional.of(user));
        doReturn("new-access-token").when(jwtService).generateToken(any(), any(), any());

        TokenRefreshResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(refreshTokenService).findByToken(request.getRefreshToken());
        verify(refreshTokenService).verifyExpiration(refreshToken);
        verify(userRepository).findByIdWithRoles(anyLong());
        verify(jwtService).generateToken(any(), any(), any());
    }

    @Test
    void login_WithNonExistentUser_ShouldThrowException() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmailForAuthentication(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmailForAuthentication(loginRequest.getEmail());
    }

    @Test
    void logout_ShouldAddTokenToBlacklist() {
        String token = "jwt.token.here";
        Date expiryDate = new Date(System.currentTimeMillis() + 3600000);
        when(jwtService.extractExpiration(token)).thenReturn(expiryDate);

        authService.logout(token);

        verify(jwtService).extractExpiration(token);
        verify(tokenBlacklistService).addToBlacklist(token, expiryDate);
    }

    @Test
    void logout_WithRefreshToken_ShouldAddTokenToBlacklistAndDeleteRefreshToken() {
        String token = "jwt.token.here";
        String refreshTokenStr = "refresh-token";
        Date expiryDate = new Date(System.currentTimeMillis() + 3600000);

        when(jwtService.extractExpiration(token)).thenReturn(expiryDate);
        when(refreshTokenService.findByToken(refreshTokenStr)).thenReturn(Optional.of(refreshToken));

        authService.logout(token, refreshTokenStr);

        verify(jwtService).extractExpiration(token);
        verify(tokenBlacklistService).addToBlacklist(token, expiryDate);
        verify(refreshTokenService).findByToken(refreshTokenStr);
        verify(refreshTokenService).deleteToken(refreshToken);
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowException() {
        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken("invalid-token")
                .build();

        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(TokenRefreshException.class, () -> {
            authService.refreshToken(request);
        });

        assertTrue(exception.getMessage().contains("Refresh token is not in database!"));
        verify(refreshTokenService).findByToken(request.getRefreshToken());
    }

    @Test
    void logout_WithExceptionInTokenExtraction_ShouldContinueWithRefreshToken() {
        String token = "jwt.token.here";
        String refreshTokenStr = "refresh-token";

        when(jwtService.extractExpiration(token)).thenThrow(new RuntimeException("Token extraction failed"));
        when(refreshTokenService.findByToken(refreshTokenStr)).thenReturn(Optional.of(refreshToken));

        authService.logout(token, refreshTokenStr);

        verify(jwtService).extractExpiration(token);
        verify(tokenBlacklistService, never()).addToBlacklist(any(), any());
        verify(refreshTokenService).findByToken(refreshTokenStr);
        verify(refreshTokenService).deleteToken(refreshToken);
    }

    @Test
    void register_WithRoleNotFound_ShouldThrowException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(any(RoleType.class))).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Error: Role STUDENT not found.", exception.getMessage());
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(roleRepository).findByName(RoleType.STUDENT);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void refreshToken_WithUserNotFoundForRefresh_ShouldThrowException() {
        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken("refresh-token")
                .build();

        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(any(RefreshToken.class))).thenReturn(refreshToken);
        when(userRepository.findByIdWithRoles(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(request);
        });

        assertEquals("User not found", exception.getMessage());
        verify(refreshTokenService).findByToken(request.getRefreshToken());
        verify(refreshTokenService).verifyExpiration(refreshToken);
        verify(userRepository).findByIdWithRoles(anyLong());
    }
}