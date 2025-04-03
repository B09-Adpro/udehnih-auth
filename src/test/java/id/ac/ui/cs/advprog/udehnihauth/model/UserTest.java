package id.ac.ui.cs.advprog.udehnihauth.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private final UUID testId = UUID.randomUUID();
    private final String testEmail = "test@example.com";
    private final String testFullName = "Test User";
    private final String testPassword = "password123";
    private final LocalDateTime testRegistrationDate = LocalDateTime.now();
    private final String testPhoneNumber = "1234567890";
    private final LocalDateTime testDateOfBirth = LocalDateTime.of(2000, 1, 1, 0, 0);
    private final Gender testGender = Gender.MALE;

    @BeforeEach
    void setUp() {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.STUDENT);

        user = User.builder()
                .id(testId)
                .email(testEmail)
                .fullName(testFullName)
                .password(testPassword)
                .registrationDate(testRegistrationDate)
                .phoneNumber(testPhoneNumber)
                .dateOfBirth(testDateOfBirth)
                .gender(testGender)
                .roles(roles)
                .build();
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals(testId, user.getId());
        assertEquals(testEmail, user.getEmail());
        assertEquals(testFullName, user.getFullName());
        assertEquals(testPassword, user.getPassword());
        assertEquals(testRegistrationDate, user.getRegistrationDate());
        assertEquals(testPhoneNumber, user.getPhoneNumber());
        assertEquals(testDateOfBirth, user.getDateOfBirth());
        assertEquals(testGender, user.getGender());
        assertTrue(user.getRoles().contains(Role.STUDENT));
    }

    @Test
    void testAddRole() {
        assertFalse(user.hasRole(Role.TUTOR));

        user.addRole(Role.TUTOR);

        assertTrue(user.hasRole(Role.TUTOR));
        assertEquals(2, user.getRoles().size());
    }

    @Test
    void testHasRole() {
        assertTrue(user.hasRole(Role.STUDENT));
        assertFalse(user.hasRole(Role.ADMIN));
    }

    @Test
    void testUserBuilder() {
        User newUser = User.builder()
                .email("new@example.com")
                .fullName("New User")
                .password("newpass123")
                .build();

        assertNotNull(newUser);
        assertEquals("new@example.com", newUser.getEmail());
        assertEquals("New User", newUser.getFullName());
        assertEquals("newpass123", newUser.getPassword());
        assertNotNull(newUser.getRoles(), "Roles set should be initialized");
        assertTrue(newUser.getRoles().isEmpty(), "Roles set should be empty");
    }

    @Test
    void testNoArgsConstructor() {
        User emptyUser = new User();

        assertNotNull(emptyUser);
        assertNull(emptyUser.getId());
        assertNull(emptyUser.getEmail());
        assertNull(emptyUser.getFullName());
        assertNull(emptyUser.getPassword());
        assertNull(emptyUser.getRegistrationDate());
        assertNull(emptyUser.getPhoneNumber());
        assertNull(emptyUser.getDateOfBirth());
        assertNull(emptyUser.getGender());
        assertNotNull(emptyUser.getRoles(), "Roles set should be initialized");
    }
}