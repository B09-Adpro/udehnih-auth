package id.ac.ui.cs.advprog.udehnihauth.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private Role studentRole;
    private final String email = "test@example.com";
    private final String name = "Test User";
    private final String password = "password123";

    @BeforeEach
    void setUp() {
        studentRole = new Role();
        studentRole.setId(1L);
        studentRole.setName(RoleType.STUDENT);
        studentRole.setUsers(new HashSet<>());

        user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setName(name);
        user.setPassword(password);
        user.setRegistrationDate(LocalDateTime.now());
        user.setRoles(new HashSet<>());

        user.getRoles().add(studentRole);
        studentRole.getUsers().add(user);
    }

    @Test
    void testUserProperties() {
        assertEquals(1L, user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(name, user.getName());
        assertEquals(password, user.getPassword());
        assertNotNull(user.getRegistrationDate());
        assertEquals(1, user.getRoles().size());

        assertTrue(user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.STUDENT));
    }

    @Test
    void testEqualsAndHashCode() {
        User sameUser = new User();
        sameUser.setId(1L);
        sameUser.setEmail("different@example.com");

        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setEmail(email);

        assertEquals(user, sameUser);
        assertEquals(user.hashCode(), sameUser.hashCode());

        assertNotEquals(user, differentUser);
        assertNotEquals(user.hashCode(), differentUser.hashCode());
    }

    @Test
    void testRoleRelationship() {
        assertEquals(1, user.getRoles().size());
        assertEquals(1, studentRole.getUsers().size());
        assertTrue(studentRole.getUsers().contains(user));

        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setRoles(new HashSet<>());

        studentRole.getUsers().add(anotherUser);
        anotherUser.getRoles().add(studentRole);

        assertEquals(2, studentRole.getUsers().size());
        assertTrue(studentRole.getUsers().contains(anotherUser));
    }
}