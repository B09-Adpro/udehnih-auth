package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.dto.request.LoginRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.LogoutRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.RegisterRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.AuthResponse;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.ServiceResponse;

public interface AuthService {
    ServiceResponse<AuthResponse> register(RegisterRequest request);
    ServiceResponse<AuthResponse> authenticate(LoginRequest request);
    ServiceResponse<Boolean> validateToken(String token);
    ServiceResponse<AuthResponse> refreshToken(String refreshToken);
    ServiceResponse<Void> logout(LogoutRequest logoutRequest);
}