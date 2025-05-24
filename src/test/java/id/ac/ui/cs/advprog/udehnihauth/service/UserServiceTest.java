package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.dto.response.UserInfoResponse;
import id.ac.ui.cs.advprog.udehnihauth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.udehnihauth.model.User;
import id.ac.ui.cs.advprog.udehnihauth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private final Long userId = 1L;
    private final String userIdStr = "1";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .name("Test User")
                .password("encodedPassword")
                .registrationDate(LocalDateTime.now())
                .build();
    }

    @Test
    void getUserInfo_ExistingUser_ReturnsUserInfo() {
        when(userRepository.findByIdForProfile(anyLong())).thenReturn(Optional.of(user));

        UserInfoResponse response = userService.getUserInfo(userId);

        assertNotNull(response);
        assertEquals(userIdStr, response.getId());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getName(), response.getName());

        verify(userRepository).findByIdForProfile(userId);
    }

    @Test
    void getUserInfo_NonExistingUser_ThrowsException() {
        when(userRepository.findByIdForProfile(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserInfo(userId);
        });

        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository).findByIdForProfile(userId);
    }

    @Test
    void getUserInfoWithRoles_ExistingUser_ReturnsUserInfo() {
        when(userRepository.findByIdWithRoles(anyLong())).thenReturn(Optional.of(user));

        UserInfoResponse response = userService.getUserInfoWithRoles(userId);

        assertNotNull(response);
        assertEquals(userIdStr, response.getId());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getName(), response.getName());

        verify(userRepository).findByIdWithRoles(userId);
    }

    @Test
    void getUserInfoWithRoles_NonExistingUser_ThrowsException() {
        when(userRepository.findByIdWithRoles(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserInfoWithRoles(userId);
        });

        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository).findByIdWithRoles(userId);
    }

    @Test
    void getUserInfo_WithNullUser_ThrowsException() {
        when(userRepository.findByIdForProfile(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserInfo(userId);
        });
    }

    @Test
    void getUserInfo_WithValidData_MapsCorrectly() {
        User userWithSpecialCharacters = User.builder()
                .id(2L)
                .email("test+user@example.com")
                .name("Test User With Special Characters äöü")
                .password("encodedPassword")
                .registrationDate(LocalDateTime.now())
                .build();

        when(userRepository.findByIdForProfile(2L)).thenReturn(Optional.of(userWithSpecialCharacters));

        UserInfoResponse response = userService.getUserInfo(2L);

        assertNotNull(response);
        assertEquals("2", response.getId());
        assertEquals("test+user@example.com", response.getEmail());
        assertEquals("Test User With Special Characters äöü", response.getName());
    }
}