package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.config.JwtService;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.LogoutRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.RegisterRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.AuthResponse;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.ServiceResponse;
import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public ServiceResponse<AuthResponse> register(RegisterRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ServiceResponse.error("Email is required");
            }

            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ServiceResponse.error("Password is required");
            }

            if (request.getFullName() == null || request.getFullName().isEmpty()) {
                return ServiceResponse.error("Full name is required");
            }

            if (userService.existsByEmail(request.getEmail())) {
                return ServiceResponse.error("Email already registered");
            }

            User user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .phoneNumber(request.getPhoneNumber())
                    .dateOfBirth(request.getDateOfBirth())
                    .gender(request.getGender())
                    .registrationDate(LocalDateTime.now())
                    .build();

            user.addRole(Role.STUDENT);

            User savedUser = userService.saveUser(user);

            String accessToken = jwtService.generateToken(savedUser);
            String refreshToken = jwtService.generateRefreshToken(savedUser);

            AuthResponse authResponse = AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .build();

            return ServiceResponse.success(authResponse, "User registered successfully");
        } catch (Exception e) {
            return ServiceResponse.error("Registration failed: " + e.getMessage());
        }
    }

    @Override
    public ServiceResponse<AuthResponse> authenticate(LoginRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ServiceResponse.error("Email is required");
            }

            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ServiceResponse.error("Password is required");
            }

            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );
            } catch (BadCredentialsException e) {
                return ServiceResponse.error("Invalid email or password");
            }

            Optional<User> userOptional = userService.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                return ServiceResponse.error("User not found");
            }
            User user = userOptional.get();

            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            AuthResponse authResponse = AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .build();

            return ServiceResponse.success(authResponse, "Authentication successful");
        } catch (Exception e) {
            return ServiceResponse.error("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public ServiceResponse<Boolean> validateToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return ServiceResponse.error("Token is required");
            }

            boolean isValid = jwtService.isTokenValid(token);
            return ServiceResponse.success(isValid, isValid ? "Token is valid" : "Token is invalid");
        } catch (Exception e) {
            return ServiceResponse.error("Token validation failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ServiceResponse<AuthResponse> refreshToken(String refreshToken) {
        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ServiceResponse.error("Refresh token is required");
            }

            if (!jwtService.isTokenValid(refreshToken)) {
                return ServiceResponse.error("Invalid refresh token");
            }

            String email = jwtService.extractUsername(refreshToken);
            if (email == null) {
                return ServiceResponse.error("Cannot extract user from token");
            }

            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isEmpty()) {
                return ServiceResponse.error("User not found");
            }
            User user = userOptional.get();

            String accessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            AuthResponse authResponse = AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(newRefreshToken)
                    .build();

            return ServiceResponse.success(authResponse, "Token refreshed successfully");
        } catch (Exception e) {
            return ServiceResponse.error("Token refresh failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ServiceResponse<Void> logout(LogoutRequest logoutRequest) {
        try {
            if (logoutRequest == null) {
                return ServiceResponse.error("Logout request is required");
            }

            String accessToken = logoutRequest.getAccessToken();
            if (accessToken != null && accessToken.startsWith("Bearer ")) {
                accessToken = accessToken.substring(7);
            }

            String refreshToken = logoutRequest.getRefreshToken();

            boolean accessTokenRevoked = false;
            boolean refreshTokenRevoked = false;

            if (refreshToken != null && !refreshToken.isEmpty()) {
                if (jwtService.isTokenValid(refreshToken)) {
                    jwtService.revokeToken(refreshToken);
                    refreshTokenRevoked = true;
                }
            }

            if (accessToken != null && !accessToken.isEmpty()) {
                if (jwtService.isTokenValid(accessToken)) {
                    jwtService.revokeToken(accessToken);
                    accessTokenRevoked = true;
                }
            }

            if (!accessTokenRevoked && !refreshTokenRevoked) {
                return ServiceResponse.error("No valid tokens to revoke");
            }

            return ServiceResponse.success(null, "Logout successful");
        } catch (Exception e) {
            return ServiceResponse.error("Logout failed: " + e.getMessage());
        }
    }
}