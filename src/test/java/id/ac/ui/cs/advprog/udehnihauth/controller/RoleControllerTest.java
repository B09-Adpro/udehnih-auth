package id.ac.ui.cs.advprog.udehnihauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.RoleRequest;
import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import id.ac.ui.cs.advprog.udehnihauth.service.JwtService;
import id.ac.ui.cs.advprog.udehnihauth.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RoleController roleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RoleRequest roleRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController).build();
        objectMapper = new ObjectMapper();

        roleRequest = RoleRequest.builder()
                .userId(1L)
                .roleType(RoleType.TUTOR)
                .build();
    }

    @Test
    void getUserRoles_WithValidUserId_ShouldReturnUserRoles() throws Exception {
        Set<RoleType> roles = Set.of(RoleType.STUDENT, RoleType.TUTOR);
        when(roleService.getUserRoles(1L)).thenReturn(roles);

        mockMvc.perform(get("/api/roles/user/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.student").value(true))  // Changed from isStudent
                .andExpect(jsonPath("$.tutor").value(true))    // Changed from isTutor
                .andExpect(jsonPath("$.staff").value(false));  // Changed from isStaff
    }

    @Test
    void getUserRoles_WithEmptyRoles_ShouldReturnEmptyRoles() throws Exception {
        Set<RoleType> roles = Set.of();
        when(roleService.getUserRoles(1L)).thenReturn(roles);

        mockMvc.perform(get("/api/roles/user/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.roles").isEmpty())
                .andExpect(jsonPath("$.student").value(false))  // Changed from isStudent
                .andExpect(jsonPath("$.tutor").value(false))    // Changed from isTutor
                .andExpect(jsonPath("$.staff").value(false));   // Changed from isStaff
    }

    @Test
    void addRole_WithValidRequest_ShouldReturnSuccess() throws Exception {
        when(jwtService.extractUserId(anyString())).thenReturn(2L);
        when(roleService.addRoleToUser(1L, RoleType.TUTOR, 2L)).thenReturn(true);

        mockMvc.perform(post("/api/roles/add")
                        .header("Authorization", "Bearer jwt.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role TUTOR successfully added to user"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.roleType").value("TUTOR"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void addRole_WithFailedOperation_ShouldReturnFailure() throws Exception {
        when(jwtService.extractUserId(anyString())).thenReturn(2L);
        when(roleService.addRoleToUser(1L, RoleType.TUTOR, 2L)).thenReturn(false);

        mockMvc.perform(post("/api/roles/add")
                        .header("Authorization", "Bearer jwt.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to add role TUTOR to user or user already has this role"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.roleType").value("TUTOR"));
    }

    @Test
    void addRole_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        RoleRequest invalidRequest = RoleRequest.builder()
                .userId(null)
                .roleType(RoleType.TUTOR)
                .build();

        mockMvc.perform(post("/api/roles/add")
                        .header("Authorization", "Bearer jwt.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeRole_WithValidRequest_ShouldReturnSuccess() throws Exception {
        when(jwtService.extractUserId(anyString())).thenReturn(2L);
        when(roleService.removeRoleFromUser(1L, RoleType.TUTOR)).thenReturn(true);

        mockMvc.perform(post("/api/roles/remove")
                        .header("Authorization", "Bearer jwt.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role TUTOR successfully removed from user"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.roleType").value("TUTOR"));
    }

    @Test
    void removeRole_WithFailedOperation_ShouldReturnFailure() throws Exception {
        when(jwtService.extractUserId(anyString())).thenReturn(2L);
        when(roleService.removeRoleFromUser(1L, RoleType.TUTOR)).thenReturn(false);

        mockMvc.perform(post("/api/roles/remove")
                        .header("Authorization", "Bearer jwt.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to remove role TUTOR from user or user doesn't have this role"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.roleType").value("TUTOR"));
    }

    @Test
    void checkUserRole_WithExistingRole_ShouldReturnTrue() throws Exception {
        when(roleService.userHasRole(1L, RoleType.STUDENT)).thenReturn(true);

        mockMvc.perform(get("/api/roles/check")
                        .param("userId", "1")
                        .param("roleType", "STUDENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));
    }

    @Test
    void checkUserRole_WithNonExistingRole_ShouldReturnFalse() throws Exception {
        when(roleService.userHasRole(1L, RoleType.TUTOR)).thenReturn(false);

        mockMvc.perform(get("/api/roles/check")
                        .param("userId", "1")
                        .param("roleType", "TUTOR")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("false"));
    }

    @Test
    void addRole_WithMissingAuthHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeRole_WithMissingAuthHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/roles/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserRoles_WithStaffOnly_ShouldReturnCorrectFlags() throws Exception {
        Set<RoleType> roles = Set.of(RoleType.STAFF);
        when(roleService.getUserRoles(1L)).thenReturn(roles);

        mockMvc.perform(get("/api/roles/user/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.student").value(false))
                .andExpect(jsonPath("$.tutor").value(false))
                .andExpect(jsonPath("$.staff").value(true));
    }

    @Test
    void addRole_WithStaffRole_ShouldReturnSuccess() throws Exception {
        RoleRequest staffRoleRequest = RoleRequest.builder()
                .userId(1L)
                .roleType(RoleType.STAFF)
                .build();

        when(jwtService.extractUserId(anyString())).thenReturn(2L);
        when(roleService.addRoleToUser(1L, RoleType.STAFF, 2L)).thenReturn(true);

        mockMvc.perform(post("/api/roles/add")
                        .header("Authorization", "Bearer jwt.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(staffRoleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role STAFF successfully added to user"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.roleType").value("STAFF"));
    }
}