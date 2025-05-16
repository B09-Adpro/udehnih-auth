package id.ac.ui.cs.advprog.udehnihauth.service;

import id.ac.ui.cs.advprog.udehnihauth.dto.response.UserInfoResponse;
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
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserInfoResponse response = userService.getUserInfo(userId);

        assertNotNull(response);
        assertEquals(userIdStr, response.getId());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getName(), response.getName());
    }

    @Test
    void getUserInfo_NonExistingUser_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserInfo(userId);
        });

        assertEquals("User not found with id: " + userId, exception.getMessage());
    }
}