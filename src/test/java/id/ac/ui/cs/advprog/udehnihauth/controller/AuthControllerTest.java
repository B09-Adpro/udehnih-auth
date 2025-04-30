package id.ac.ui.cs.advprog.udehnihauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.TokenRefreshRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.AuthResponse;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.RegisterRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.TokenRefreshResponse;
import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import id.ac.ui.cs.advprog.udehnihauth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .name("Test User")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        Set<RoleType> roles = new HashSet<>();
        roles.add(RoleType.STUDENT);

        authResponse = AuthResponse.builder()
                .token("jwt.token.here")
                .email("test@example.com")
                .name("Test User")
                .roles(roles)
                .build();
    }

    @Test
    void testRegister() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(authResponse.getToken()))
                .andExpect(jsonPath("$.email").value(authResponse.getEmail()))
                .andExpect(jsonPath("$.name").value(authResponse.getName()));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void testLogin() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(authResponse.getToken()))
                .andExpect(jsonPath("$.email").value(authResponse.getEmail()))
                .andExpect(jsonPath("$.name").value(authResponse.getName()));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void testLogout() throws Exception {
        String token = "Bearer jwt.token.here";

        doNothing().when(authService).logout(anyString(), isNull());

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful. Your session has been terminated."));

        verify(authService).logout(anyString(), isNull());
    }

    @Test
    void testRefreshToken() throws Exception {
        TokenRefreshRequest refreshRequest = TokenRefreshRequest.builder()
                .refreshToken("refresh-token")
                .build();

        TokenRefreshResponse refreshResponse = TokenRefreshResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("refresh-token")
                .build();

        when(authService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(refreshResponse);

        mockMvc.perform(post("/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value(refreshResponse.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(refreshResponse.getRefreshToken()));

        verify(authService).refreshToken(any(TokenRefreshRequest.class));
    }
}