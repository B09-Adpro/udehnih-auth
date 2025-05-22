package id.ac.ui.cs.advprog.udehnihauth.controller;

import id.ac.ui.cs.advprog.udehnihauth.dto.request.RoleRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.RoleResponse;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.UserRolesResponse;
import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import id.ac.ui.cs.advprog.udehnihauth.service.JwtService;
import id.ac.ui.cs.advprog.udehnihauth.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final JwtService jwtService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserRolesResponse> getUserRoles(@PathVariable Long userId) {
        Set<RoleType> roles = roleService.getUserRoles(userId);

        UserRolesResponse response = UserRolesResponse.builder()
                .userId(userId)
                .roles(roles)
                .isStaff(roles.contains(RoleType.STAFF))
                .isTutor(roles.contains(RoleType.TUTOR))
                .isStudent(roles.contains(RoleType.STUDENT))
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<RoleResponse> addRole(
            @Valid @RequestBody RoleRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long staffId = jwtService.extractUserId(token);

        boolean success = roleService.addRoleToUser(request.getUserId(), request.getRoleType(), staffId);

        String message = success ?
                String.format("Role %s successfully added to user", request.getRoleType()) :
                String.format("Failed to add role %s to user or user already has this role", request.getRoleType());

        RoleResponse response = RoleResponse.builder()
                .success(success)
                .message(message)
                .userId(request.getUserId())
                .roleType(request.getRoleType())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<RoleResponse> removeRole(
            @Valid @RequestBody RoleRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long staffId = jwtService.extractUserId(token);

        boolean success = roleService.removeRoleFromUser(request.getUserId(), request.getRoleType());

        String message = success ?
                String.format("Role %s successfully removed from user", request.getRoleType()) :
                String.format("Failed to remove role %s from user or user doesn't have this role", request.getRoleType());

        RoleResponse response = RoleResponse.builder()
                .success(success)
                .message(message)
                .userId(request.getUserId())
                .roleType(request.getRoleType())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkUserRole(
            @RequestParam Long userId,
            @RequestParam RoleType roleType) {

        boolean hasRole = roleService.userHasRole(userId, roleType);
        return ResponseEntity.ok(hasRole);
    }
}