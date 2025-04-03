package id.ac.ui.cs.advprog.udehnihauth.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    @Test
    void testLoginRequestCreation() {
        String email = "test@example.com";
        String password = "password123";

        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        assertNotNull(request);
        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
    }

    @Test
    void testNoArgsConstructor() {
        LoginRequest request = new LoginRequest();

        assertNotNull(request);
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void testSetters() {
        LoginRequest request = new LoginRequest();
        String email = "test@example.com";
        String password = "password123";

        request.setEmail(email);
        request.setPassword(password);

        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
    }

    @Test
    void testIfLoginEqualsOrNot() {
        LoginRequest request1 = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        LoginRequest request2 = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        LoginRequest request3 = LoginRequest.builder()
                .email("different@example.com")
                .password("password123")
                .build();

        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
    }

    @Test
    void testHashCode() {
        LoginRequest request1 = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        LoginRequest request2 = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        String toString = request.toString();

        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("password123"));
    }
}
