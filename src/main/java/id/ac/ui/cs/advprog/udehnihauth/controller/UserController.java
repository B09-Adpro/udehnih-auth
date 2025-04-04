package id.ac.ui.cs.advprog.udehnihauth.controller;

import id.ac.ui.cs.advprog.udehnihauth.dto.request.UpdateUserRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.ServiceResponse;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.UserResponse;
import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import id.ac.ui.cs.advprog.udehnihauth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        Optional<User> userOptional = userService.findByEmail(authentication.getName());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok(userService.convertToDto(userOptional.get()));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(
            Authentication authentication,
            @RequestBody @Valid UpdateUserRequest request
    ) {
        Optional<User> userOptional = userService.findByEmail(authentication.getName());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOptional.get();

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        User updatedUser = userService.saveUser(user);
        return ResponseEntity.ok(userService.convertToDto(updatedUser));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        List<UserResponse> userResponses = users.stream()
                .map(userService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        Optional<User> userOptional = userService.findById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok(userService.convertToDto(userOptional.get()));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable Role role) {
        List<User> users = userService.findUsersByRole(role);
        List<UserResponse> userResponses = users.stream()
                .map(userService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    @PostMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addRoleToUser(
            @PathVariable UUID id,
            @PathVariable Role role
    ) {
        ServiceResponse<User> response = userService.addRoleToUser(id, role);

        if (response.isSuccess()) {
            return ResponseEntity.ok(userService.convertToDto(response.getData()));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        ServiceResponse<Void> response = userService.deleteUser(id);

        if (response.isSuccess()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMessage());
        }
    }
}