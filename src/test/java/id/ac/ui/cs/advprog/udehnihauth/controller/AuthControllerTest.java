package id.ac.ui.cs.advprog.udehnihauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.RegisterRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.AuthResponse;
import id.ac.ui.cs.advprog.udehnihauth.model.Gender;
import id.ac.ui.cs.advprog.udehnihauth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({TestSecurityConfig.class, AuthControllerTest.TestConfig.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class AuthControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthService authService() {
            return Mockito.mock(AuthService.class);
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return Mockito.mock(UserDetailsService.class);
        }
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    private MockMvc mockMvc;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .addFilter((request, response, chain) -> {
                    try {
                        chain.doFilter(request, response);
                    } catch (BadCredentialsException e) {
                        ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }, UsernamePasswordAuthenticationFilter.class.getName())
                .build();

        Mockito.reset(authService);

        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("password123")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDateTime.now().minusYears(20))
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        refreshToken = "refresh-token-value";

        authResponse = AuthResponse.builder()
                .token("access-token-value")
                .refreshToken(refreshToken)
                .build();

        doReturn(authResponse).when(authService).register(any(RegisterRequest.class));
        doReturn(authResponse).when(authService).authenticate(any(LoginRequest.class));
        doReturn(authResponse).when(authService).refreshToken(anyString());
        doNothing().when(authService).logout(anyString());
    }

    @Test
    void registerShouldReturnAuthResponse() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token-value"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-value"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void loginShouldReturnAuthResponse() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token-value"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-value"));

        verify(authService, times(1)).authenticate(any(LoginRequest.class));
    }

    @Test
    void refreshTokenShouldReturnNewAuthResponse() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + refreshToken + "\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token-value"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-value"));

        verify(authService, times(1)).refreshToken(anyString());
    }

    @Test
    void logoutShouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"" + refreshToken + "\""))
                .andExpect(status().isNoContent());

        verify(authService, times(1)).logout(anyString());
    }

    @Test
    void registerWithInvalidDataShouldReturnBadRequest() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .email("")
                .password("")
                .build();

        doThrow(new IllegalArgumentException("Invalid registration data"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithInvalidCredentialsShouldReturnUnauthorized() throws Exception {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authService).authenticate(any(LoginRequest.class));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenWithInvalidTokenShouldReturnBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Invalid refresh token"))
                .when(authService).refreshToken(anyString());

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"invalid-token\""))
                .andExpect(status().isBadRequest());
    }
}