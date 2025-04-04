package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.config.JwtService;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.LogoutRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.RegisterRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.AuthResponse;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.ServiceResponse;
import id.ac.ui.cs.advprog.udehnihauth.model.Gender;
import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private LogoutRequest logoutRequest;
    private User testUser;
    private String accessToken;
    private String refreshToken;
    private String extractedAccessToken;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .gender(Gender.MALE)
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        accessToken = "access-token";
        refreshToken = "refresh-token";
        extractedAccessToken = "access-token";

        logoutRequest = LogoutRequest.builder()
                .accessToken("Bearer " + accessToken)
                .refreshToken(refreshToken)
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .fullName("Test User")
                .password("encodedPassword")
                .registrationDate(LocalDateTime.now())
                .gender(Gender.MALE)
                .build();
        testUser.addRole(Role.STUDENT);
    }

    @Test
    void whenRegister_thenReturnSuccessServiceResponse() {
        when(userService.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(testUser)).thenReturn(refreshToken);

        ServiceResponse<AuthResponse> response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getToken()).isEqualTo(accessToken);
        assertThat(response.getData().getRefreshToken()).isEqualTo(refreshToken);
        verify(userService, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userService, times(1)).saveUser(any(User.class));
        verify(jwtService, times(1)).generateToken(testUser);
        verify(jwtService, times(1)).generateRefreshToken(testUser);
    }

    @Test
    void whenRegisterWithExistingEmail_thenReturnErrorServiceResponse() {
        when(userService.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        ServiceResponse<AuthResponse> response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Email already registered");
        verify(userService, times(1)).existsByEmail(registerRequest.getEmail());
        verify(userService, never()).saveUser(any(User.class));
    }

    @Test
    void whenAuthenticate_thenReturnSuccessServiceResponse() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(testUser)).thenReturn(refreshToken);

        ServiceResponse<AuthResponse> response = authService.authenticate(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getToken()).isEqualTo(accessToken);
        assertThat(response.getData().getRefreshToken()).isEqualTo(refreshToken);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, times(1)).findByEmail(loginRequest.getEmail());
        verify(jwtService, times(1)).generateToken(testUser);
        verify(jwtService, times(1)).generateRefreshToken(testUser);
    }

    @Test
    void whenAuthenticateWithInvalidCredentials_thenReturnErrorServiceResponse() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        ServiceResponse<AuthResponse> response = authService.authenticate(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Invalid email or password");
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, never()).findByEmail(any());
    }

    @Test
    void whenAuthenticateWithNonExistentUser_thenReturnErrorServiceResponse() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        ServiceResponse<AuthResponse> response = authService.authenticate(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("User not found");
        verify(authenticationManager, times(1)).authenticate(any());
        verify(userService, times(1)).findByEmail(loginRequest.getEmail());
    }

    @Test
    void whenValidateToken_thenReturnSuccessServiceResponse() {
        when(jwtService.isTokenValid(accessToken)).thenReturn(true);

        ServiceResponse<Boolean> response = authService.validateToken(accessToken);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isTrue();
        verify(jwtService, times(1)).isTokenValid(accessToken);
    }

    @Test
    void whenValidateInvalidToken_thenReturnSuccessServiceResponseWithFalseData() {
        when(jwtService.isTokenValid(accessToken)).thenReturn(false);

        ServiceResponse<Boolean> response = authService.validateToken(accessToken);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isFalse();
        verify(jwtService, times(1)).isTokenValid(accessToken);
    }

    @Test
    void whenRefreshToken_thenReturnSuccessServiceResponse() {
        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("new-refresh-token");

        ServiceResponse<AuthResponse> response = authService.refreshToken(refreshToken);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getToken()).isEqualTo("new-access-token");
        assertThat(response.getData().getRefreshToken()).isEqualTo("new-refresh-token");
        verify(jwtService, times(1)).isTokenValid(refreshToken);
        verify(jwtService, times(1)).extractUsername(refreshToken);
        verify(userService, times(1)).findByEmail("test@example.com");
        verify(jwtService, times(1)).generateToken(testUser);
        verify(jwtService, times(1)).generateRefreshToken(testUser);
    }

    @Test
    void whenRefreshInvalidToken_thenReturnErrorServiceResponse() {
        when(jwtService.isTokenValid(refreshToken)).thenReturn(false);

        ServiceResponse<AuthResponse> response = authService.refreshToken(refreshToken);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Invalid refresh token");
        verify(jwtService, times(1)).isTokenValid(refreshToken);
    }

    @Test
    void whenRefreshTokenWithNonExistentUser_thenReturnErrorServiceResponse() {
        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());

        ServiceResponse<AuthResponse> response = authService.refreshToken(refreshToken);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("User not found");
        verify(jwtService, times(1)).isTokenValid(refreshToken);
        verify(jwtService, times(1)).extractUsername(refreshToken);
        verify(userService, times(1)).findByEmail("test@example.com");
    }

    @Test
    void whenLogout_thenReturnSuccessServiceResponse() {
        doAnswer(invocation -> {
            String token = invocation.getArgument(0);
            if (token.startsWith("Bearer ")) {
                return true;
            }
            return token.equals(refreshToken) || token.equals(extractedAccessToken);
        }).when(jwtService).isTokenValid(anyString());

        when(jwtService.isTokenValid(extractedAccessToken)).thenReturn(true);
        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);

        doNothing().when(jwtService).revokeToken(extractedAccessToken);
        doNothing().when(jwtService).revokeToken(refreshToken);

        ServiceResponse<Void> response = authService.logout(logoutRequest);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("Logout successful");
        verify(jwtService, atLeastOnce()).isTokenValid(anyString());
        verify(jwtService, atLeastOnce()).revokeToken(anyString());
    }

    @Test
    void whenLogoutWithInvalidTokens_thenReturnErrorServiceResponse() {
        when(jwtService.isTokenValid(anyString())).thenReturn(false);

        ServiceResponse<Void> response = authService.logout(logoutRequest);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("No valid tokens to revoke");
        verify(jwtService, atLeastOnce()).isTokenValid(anyString());
        verify(jwtService, never()).revokeToken(anyString());
    }
}