package id.ac.ui.cs.advprog.udehnihauth.controller;

import id.ac.ui.cs.advprog.udehnihauth.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.LogoutRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.RegisterRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.ServiceResponse;
import id.ac.ui.cs.advprog.udehnihauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        ServiceResponse<?> response = authService.register(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response.getData());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        ServiceResponse<?> response = authService.authenticate(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response.getData());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody String refreshToken) {
        ServiceResponse<?> response = authService.refreshToken(refreshToken);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response.getData());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest logoutRequest) {
        ServiceResponse<Void> response = authService.logout(logoutRequest);

        if (response.isSuccess()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMessage());
        }
    }
}