package id.ac.ui.cs.advprog.udehnihauth.controller;

import id.ac.ui.cs.advprog.udehnihauth.dto.response.UserInfoResponse;
import id.ac.ui.cs.advprog.udehnihauth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.udehnihauth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private UserInfoResponse userInfoResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        userInfoResponse = UserInfoResponse.builder()
                .id("1")
                .email("test@example.com")
                .name("Test User")
                .build();
    }

    @Test
    void getUserInfo_ExistingUser_ReturnsUserInfo() throws Exception {
        when(userService.getUserInfo(anyLong())).thenReturn(userInfoResponse);

        mockMvc.perform(get("/api/users/{userId}", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userInfoResponse.getId()))
                .andExpect(jsonPath("$.email").value(userInfoResponse.getEmail()))
                .andExpect(jsonPath("$.name").value(userInfoResponse.getName()));
    }

    @Test
    void getUserInfo_NonExistingUser_ReturnsNotFound() throws Exception {
        when(userService.getUserInfo(anyLong())).thenThrow(new UserNotFoundException("User not found with id: 999"));

        mockMvc.perform(get("/api/users/{userId}", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}