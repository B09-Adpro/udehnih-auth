package id.ac.ui.cs.advprog.udehnihauth.dto.response;

import id.ac.ui.cs.advprog.udehnihauth.model.Gender;
import id.ac.ui.cs.advprog.udehnihauth.model.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseTest {

    @Test
    void testUserResponseCreation() {
        // Given
        String id = UUID.randomUUID().toString();
        String email = "test@example.com";
        String fullName = "Test User";
        String phoneNumber = "1234567890";
        LocalDateTime registrationDate = LocalDateTime.now();
        Set<Role> roles = new HashSet<>();
        roles.add(Role.STUDENT);
        LocalDateTime dateOfBirth = LocalDateTime.of(2000, 1, 1, 0, 0);
        Gender gender = Gender.MALE;

        // When
        UserResponse response = UserResponse.builder()
                .id(id)
                .email(email)
                .fullName(fullName)
                .phoneNumber(phoneNumber)
                .registrationDate(registrationDate)
                .roles(roles)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .build();

        // Then
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(email, response.getEmail());
        assertEquals(fullName, response.getFullName());
        assertEquals(phoneNumber, response.getPhoneNumber());
        assertEquals(registrationDate, response.getRegistrationDate());
        assertEquals(roles, response.getRoles());
        assertEquals(dateOfBirth, response.getDateOfBirth());
        assertEquals(gender, response.getGender());
    }

    @Test
    void testNoArgsConstructor() {
        // When
        UserResponse response = new UserResponse();

        // Then
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getEmail());
        assertNull(response.getFullName());
        assertNull(response.getPhoneNumber());
        assertNull(response.getRegistrationDate());
        assertNull(response.getRoles());
        assertNull(response.getDateOfBirth());
        assertNull(response.getGender());
    }

    @Test
    void testSetters() {
        // Given
        UserResponse response = new UserResponse();
        String id = UUID.randomUUID().toString();
        String email = "test@example.com";
        String fullName = "Test User";
        String phoneNumber = "1234567890";
        LocalDateTime registrationDate = LocalDateTime.now();
        Set<Role> roles = new HashSet<>();
        roles.add(Role.STUDENT);
        LocalDateTime dateOfBirth = LocalDateTime.of(2000, 1, 1, 0, 0);
        Gender gender = Gender.MALE;

        // When
        response.setId(id);
        response.setEmail(email);
        response.setFullName(fullName);
        response.setPhoneNumber(phoneNumber);
        response.setRegistrationDate(registrationDate);
        response.setRoles(roles);
        response.setDateOfBirth(dateOfBirth);
        response.setGender(gender);

        // Then
        assertEquals(id, response.getId());
        assertEquals(email, response.getEmail());
        assertEquals(fullName, response.getFullName());
        assertEquals(phoneNumber, response.getPhoneNumber());
        assertEquals(registrationDate, response.getRegistrationDate());
        assertEquals(roles, response.getRoles());
        assertEquals(dateOfBirth, response.getDateOfBirth());
        assertEquals(gender, response.getGender());
    }

    @Test
    void testEquals() {
        // Given
        String id = UUID.randomUUID().toString();
        UserResponse response1 = UserResponse.builder()
                .id(id)
                .email("test@example.com")
                .fullName("Test User")
                .build();

        UserResponse response2 = UserResponse.builder()
                .id(id)
                .email("test@example.com")
                .fullName("Test User")
                .build();

        UserResponse response3 = UserResponse.builder()
                .id(UUID.randomUUID().toString())
                .email("test@example.com")
                .fullName("Test User")
                .build();

        // Then
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
    }

    @Test
    void testHashCode() {
        // Given
        String id = UUID.randomUUID().toString();
        UserResponse response1 = UserResponse.builder()
                .id(id)
                .email("test@example.com")
                .fullName("Test User")
                .build();

        UserResponse response2 = UserResponse.builder()
                .id(id)
                .email("test@example.com")
                .fullName("Test User")
                .build();

        // Then
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        String id = UUID.randomUUID().toString();
        String email = "test@example.com";
        String fullName = "Test User";
        UserResponse response = UserResponse.builder()
                .id(id)
                .email(email)
                .fullName(fullName)
                .build();

        // When
        String toString = response.toString();

        // Then
        assertTrue(toString.contains(id));
        assertTrue(toString.contains(email));
        assertTrue(toString.contains(fullName));
    }
}
