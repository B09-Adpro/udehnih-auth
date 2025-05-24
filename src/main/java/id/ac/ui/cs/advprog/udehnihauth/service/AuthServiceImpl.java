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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Starting user registration for email: {}", request.getEmail());
        long startTime = System.currentTimeMillis();

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        Role studentRole = roleRepository.findByName(RoleType.STUDENT)
                .orElseThrow(() -> new RuntimeException("Error: Role STUDENT not found."));

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .registrationDate(LocalDateTime.now())
                .roles(new HashSet<>())
                .build();

        user.addRole(studentRole);

        User savedUser = userRepository.save(user);

        String jwtToken = jwtService.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                createUserDetails(savedUser)
        );

        AuthResponse response = buildAuthResponse(savedUser, jwtToken);

        log.info("User registration completed in {}ms for email: {}",
                System.currentTimeMillis() - startTime, request.getEmail());

        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Starting user login for email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmailForAuthentication(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String jwtToken = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                createUserDetails(user)
        );

        return buildAuthResponse(user, jwtToken);
    }

    @Override
    public void logout(String token) {
        logout(token, null);
    }

    @Override
    public void logout(String token, String refreshToken) {
        if (token != null) {
            try {
                Date expiry = jwtService.extractExpiration(token);
                tokenBlacklistService.addToBlacklist(token, expiry);
            } catch (Exception e) {
                log.warn("Failed to extract expiration from token during logout", e);
            }
        }

        if (refreshToken != null) {
            refreshTokenService.findByToken(refreshToken)
                    .ifPresent(refreshTokenService::deleteToken);
        }
    }

    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    User userWithRoles = userRepository.findByIdWithRoles(user.getId())
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    String token = jwtService.generateToken(
                            userWithRoles.getId(),
                            userWithRoles.getEmail(),
                            createUserDetails(userWithRoles)
                    );

                    return TokenRefreshResponse.builder()
                            .accessToken(token)
                            .refreshToken(requestRefreshToken)
                            .build();
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> "ROLE_" + role.getName().name())
                        .toArray(String[]::new))
                .build();
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID must not be null when creating a refresh token");
        }

        Set<RoleType> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(roles)
                .build();
    }
}