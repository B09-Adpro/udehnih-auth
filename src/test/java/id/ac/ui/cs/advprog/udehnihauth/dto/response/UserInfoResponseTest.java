package id.ac.ui.cs.advprog.udehnihauth.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserInfoResponseTest {

    @Test
    void testUserInfoResponse() {
        String id = "1";
        String email = "test@example.com";
        String name = "Test User";

        UserInfoResponse response = UserInfoResponse.builder()
                .id(id)
                .email(email)
                .name(name)
                .build();

        assertEquals(id, response.getId());
        assertEquals(email, response.getEmail());
        assertEquals(name, response.getName());
    }

    @Test
    void testUserInfoResponseNoArgsConstructor() {
        UserInfoResponse response = new UserInfoResponse();

        assertNull(response.getId());
        assertNull(response.getEmail());
        assertNull(response.getName());
    }

    @Test
    void testUserInfoResponseAllArgsConstructor() {
        String id = "1";
        String email = "test@example.com";
        String name = "Test User";

        UserInfoResponse response = new UserInfoResponse(id, email, name);

        assertEquals(id, response.getId());
        assertEquals(email, response.getEmail());
        assertEquals(name, response.getName());
    }

    @Test
    void testUserInfoResponseSetters() {
        UserInfoResponse response = new UserInfoResponse();

        response.setId("1");
        response.setEmail("test@example.com");
        response.setName("Test User");

        assertEquals("1", response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
    }
}