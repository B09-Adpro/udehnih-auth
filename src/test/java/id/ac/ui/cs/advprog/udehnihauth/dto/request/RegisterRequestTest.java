package id.ac.ui.cs.advprog.udehnihauth.dto.request;

import id.ac.ui.cs.advprog.udehnihauth.model.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    @Test
    void testRegisterRequestCreation() {
        String email = "test@example.com";
        String fullName = "Test User";
        String password = "password123";
        String phoneNumber = "1234567890";
        LocalDateTime dateOfBirth = LocalDateTime.of(2000, 1, 1, 0, 0);
        Gender gender = Gender.MALE;

        RegisterRequest request = RegisterRequest.builder()
                .email(email)
                .fullName(fullName)
                .password(password)
                .phoneNumber(phoneNumber)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .build();

        assertNotNull(request);
        assertEquals(email, request.getEmail());
        assertEquals(fullName, request.getFullName());
        assertEquals(password, request.getPassword());
        assertEquals(phoneNumber, request.getPhoneNumber());
        assertEquals(dateOfBirth, request.getDateOfBirth());
        assertEquals(gender, request.getGender());
    }

    @Test
    void testNoArgsConstructor() {
        RegisterRequest request = new RegisterRequest();

        assertNotNull(request);
        assertNull(request.getEmail());
        assertNull(request.getFullName());
        assertNull(request.getPassword());
        assertNull(request.getPhoneNumber());
        assertNull(request.getDateOfBirth());
        assertNull(request.getGender());
    }

    @Test
    void testSetters() {
        RegisterRequest request = new RegisterRequest();
        String email = "test@example.com";
        String fullName = "Test User";
        String password = "password123";
        String phoneNumber = "1234567890";
        LocalDateTime dateOfBirth = LocalDateTime.of(2000, 1, 1, 0, 0);
        Gender gender = Gender.MALE;

        request.setEmail(email);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setPhoneNumber(phoneNumber);
        request.setDateOfBirth(dateOfBirth);
        request.setGender(gender);

        assertEquals(email, request.getEmail());
        assertEquals(fullName, request.getFullName());
        assertEquals(password, request.getPassword());
        assertEquals(phoneNumber, request.getPhoneNumber());
        assertEquals(dateOfBirth, request.getDateOfBirth());
        assertEquals(gender, request.getGender());
    }

    @Test
    void testIfRegisterEqualsOrNot() {
        RegisterRequest request1 = RegisterRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("password123")
                .build();

        RegisterRequest request2 = RegisterRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("password123")
                .build();

        RegisterRequest request3 = RegisterRequest.builder()
                .email("different@example.com")
                .fullName("Test User")
                .password("password123")
                .build();

        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
    }

    @Test
    void testHashCode() {
        RegisterRequest request1 = RegisterRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("password123")
                .build();

        RegisterRequest request2 = RegisterRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("password123")
                .build();

        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .password("password123")
                .build();

        String toString = request.toString();

        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("Test User"));
        assertTrue(toString.contains("password123"));
    }
}
