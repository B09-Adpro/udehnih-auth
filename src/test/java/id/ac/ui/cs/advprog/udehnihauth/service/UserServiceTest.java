package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.dto.response.ServiceResponse;
import id.ac.ui.cs.advprog.udehnihauth.dto.response.UserResponse;
import id.ac.ui.cs.advprog.udehnihauth.model.Gender;
import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testUser = User.builder()
                .id(testId)
                .email("test@example.com")
                .fullName("Test User")
                .password("password123")
                .registrationDate(LocalDateTime.now())
                .gender(Gender.MALE)
                .build();
        testUser.addRole(Role.STUDENT);
    }

    @Test
    void whenSaveUser_thenUserIsSaved() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User savedUser = userService.saveUser(testUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isEqualTo(testId);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void whenFindById_thenReturnUser() {
        when(userRepository.findById(testId)).thenReturn(Optional.of(testUser));

        Optional<User> foundUser = userService.findById(testId);

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(testId);
        verify(userRepository, times(1)).findById(testId);
    }

    @Test
    void whenFindByEmail_thenReturnUser() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        Optional<User> foundUser = userService.findByEmail(email);

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void whenExistsByEmail_thenReturnTrue() {
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        boolean exists = userService.existsByEmail(email);

        assertThat(exists).isTrue();
        verify(userRepository, times(1)).existsByEmail(email);
    }

    @Test
    void whenFindAllUsers_thenReturnAllUsers() {
        User anotherUser = User.builder()
                .id(UUID.randomUUID())
                .email("another@example.com")
                .fullName("Another User")
                .password("password123")
                .registrationDate(LocalDateTime.now())
                .build();

        List<User> users = Arrays.asList(testUser, anotherUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> allUsers = userService.findAllUsers();

        assertThat(allUsers).hasSize(2);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void whenFindUsersByRole_thenReturnUsersWithRole() {
        List<User> students = List.of(testUser);
        when(userRepository.findByRolesContaining(Role.STUDENT)).thenReturn(students);

        List<User> foundUsers = userService.findUsersByRole(Role.STUDENT);

        assertThat(foundUsers).hasSize(1);
        assertThat(foundUsers.get(0).getRoles()).contains(Role.STUDENT);
        verify(userRepository, times(1)).findByRolesContaining(Role.STUDENT);
    }

    @Test
    void whenAddRoleToUser_thenRoleIsAddedAndReturnSuccessServiceResponse() {
        User userWithNewRole = User.builder()
                .id(testId)
                .email("test@example.com")
                .fullName("Test User")
                .password("password123")
                .registrationDate(LocalDateTime.now())
                .gender(Gender.MALE)
                .build();
        userWithNewRole.addRole(Role.STUDENT);
        userWithNewRole.addRole(Role.TUTOR);

        when(userRepository.findById(testId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(userWithNewRole);

        ServiceResponse<User> response = userService.addRoleToUser(testId, Role.TUTOR);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getRoles()).contains(Role.TUTOR);
        verify(userRepository, times(1)).findById(testId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void whenAddRoleToNonExistentUser_thenReturnErrorServiceResponse() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ServiceResponse<User> response = userService.addRoleToUser(nonExistentId, Role.TUTOR);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("User not found");
        verify(userRepository, times(1)).findById(nonExistentId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenAddExistingRoleToUser_thenReturnErrorServiceResponse() {
        when(userRepository.findById(testId)).thenReturn(Optional.of(testUser));

        ServiceResponse<User> response = userService.addRoleToUser(testId, Role.STUDENT);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("User already has role");
        verify(userRepository, times(1)).findById(testId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void whenConvertToDto_thenReturnUserResponse() {
        UserResponse userResponse = userService.convertToDto(testUser);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getId()).isEqualTo(testUser.getId());
        assertThat(userResponse.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(userResponse.getFullName()).isEqualTo(testUser.getFullName());
        assertThat(userResponse.getGender()).isEqualTo(testUser.getGender());
        assertThat(userResponse.getRoles()).containsExactlyInAnyOrderElementsOf(testUser.getRoles());
    }

    @Test
    void whenDeleteUser_thenReturnSuccessServiceResponse() {
        when(userRepository.existsById(testId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(testId);

        ServiceResponse<Void> response = userService.deleteUser(testId);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("User deleted successfully");
        verify(userRepository, times(1)).existsById(testId);
        verify(userRepository, times(1)).deleteById(testId);
    }

    @Test
    void whenDeleteNonExistentUser_thenReturnErrorServiceResponse() {
        when(userRepository.existsById(testId)).thenReturn(false);

        ServiceResponse<Void> response = userService.deleteUser(testId);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("User not found");
        verify(userRepository, times(1)).existsById(testId);
        verify(userRepository, never()).deleteById(any());
    }
}