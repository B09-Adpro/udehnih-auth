package id.ac.ui.cs.advprog.udehnihauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.udehnihauth.dto.request.UpdateUserRequest;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.UserResponse;
import id.ac.ui.cs.advprog.udehnihauth.model.Gender;
import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
import id.ac.ui.cs.advprog.udehnihauth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({TestSecurityConfig.class, UserControllerTest.TestConfig.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class UserControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private UserResponse userResponse;
    private UUID userId;
    private UpdateUserRequest updateUserRequest;
    private List<User> userList;

    @BeforeEach
    void setUp() {
        reset(userService);
        reset(userRepository);

        userId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .fullName("Test User")
                .password("encodedPassword")
                .registrationDate(LocalDateTime.now())
                .gender(Gender.MALE)
                .roles(new HashSet<>(Collections.singleton(Role.STUDENT)))
                .build();

        userResponse = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .fullName("Test User")
                .registrationDate(LocalDateTime.now())
                .gender(Gender.MALE)
                .roles(new HashSet<>(Collections.singleton(Role.STUDENT)))
                .build();

        updateUserRequest = UpdateUserRequest.builder()
                .fullName("Updated Name")
                .gender(Gender.FEMALE)
                .build();

        userList = new ArrayList<>();
        userList.add(testUser);

        User anotherUser = User.builder()
                .id(UUID.randomUUID())
                .email("another@example.com")
                .fullName("Another User")
                .password("encodedPassword")
                .registrationDate(LocalDateTime.now())
                .gender(Gender.FEMALE)
                .roles(new HashSet<>(Collections.singleton(Role.TUTOR)))
                .build();
        userList.add(anotherUser);

        doReturn(Optional.of(testUser)).when(userService).findByEmail(anyString());
        doReturn(userResponse).when(userService).convertToDto(any(User.class));
        doReturn(testUser).when(userService).saveUser(any(User.class));
        doReturn(userList).when(userService).findAllUsers();
        doReturn(Optional.of(testUser)).when(userService).findById(any(UUID.class));
        doReturn(userList).when(userService).findUsersByRole(any(Role.class));
        doReturn(testUser).when(userService).addRoleToUser(any(UUID.class), any(Role.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getCurrentUserShouldReturnUserDetails() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));

        verify(userService, times(1)).findByEmail("test@example.com");
        verify(userService, times(1)).convertToDto(any(User.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateCurrentUserShouldReturnUpdatedUser() throws Exception {
        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk());

        verify(userService, times(1)).findByEmail("test@example.com");
        verify(userService, times(1)).saveUser(any(User.class));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void getAllUsersShouldReturnListOfUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(userService, times(1)).findAllUsers();
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void getUserByIdShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));

        verify(userService, times(1)).findById(any(UUID.class));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void getUsersByRoleShouldReturnFilteredUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users/role/{role}", Role.STUDENT))
                .andExpect(status().isOk());

        verify(userService, times(1)).findUsersByRole(any(Role.class));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void addRoleToUserShouldReturnUpdatedUser() throws Exception {
        mockMvc.perform(post("/api/v1/users/{id}/roles/{role}", userId, Role.TUTOR))
                .andExpect(status().isOk());

        verify(userService, times(1)).addRoleToUser(any(UUID.class), any(Role.class));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteUserShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).findById(any(UUID.class));
        verify(userService, times(1)).deleteUser(any(UUID.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"STUDENT"})
    void studentAccessingAdminEndpointShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserShouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getCurrentUserWithNonExistentUserShouldReturnBadRequest() throws Exception {
        doReturn(Optional.empty()).when(userService).findByEmail(anyString());

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).findByEmail("test@example.com");
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void getUserByIdWithNonExistentUserShouldReturnBadRequest() throws Exception {
        doReturn(Optional.empty()).when(userService).findById(any(UUID.class));

        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()))
                .andExpect(status().isBadRequest());
    }
}