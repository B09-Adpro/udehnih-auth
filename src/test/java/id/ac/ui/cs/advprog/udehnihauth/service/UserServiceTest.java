package id.ac.ui.cs.advprog.udehnihauth.service;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void whenAddRoleToUser_thenRoleIsAdded() {
        when(userRepository.findById(testId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updatedUser = userService.addRoleToUser(testId, Role.TUTOR);

        assertThat(updatedUser.getRoles()).contains(Role.TUTOR);
        verify(userRepository, times(1)).findById(testId);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void whenAddRoleToNonExistentUser_thenThrowException() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addRoleToUser(nonExistentId, Role.TUTOR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
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
}
