package id.ac.ui.cs.advprog.udehnihauth.controller;

import id.ac.ui.cs.advprog.udehnihauth.dto.response.AuthResponse;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.RegisterRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.TokenRefreshRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.LogoutResponse;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.TokenRefreshResponse;
import id.ac.ui.cs.advprog.udehnihauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String authHeader,
                                                 @RequestParam(required = false) String refreshToken) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            authService.logout(token, refreshToken);

            LogoutResponse response = LogoutResponse.builder()
                    .message("Logout successful. Your session has been terminated.")
                    .success(true)
                    .build();

            return ResponseEntity.ok(response);
        }

        LogoutResponse response = LogoutResponse.builder()
                .message("Invalid authorization token format")
                .success(false)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}