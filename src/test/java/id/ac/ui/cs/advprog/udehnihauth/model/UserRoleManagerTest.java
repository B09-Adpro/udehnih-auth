package id.ac.ui.cs.advprog.udehnihauth.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleManagerTest {

    private User user;
    private Role studentRole;
    private Role tutorRole;
    private Role staffRole;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPassword("password123");
        user.setRegistrationDate(LocalDateTime.now());
        user.setRoles(new HashSet<>());

        studentRole = new Role(RoleType.STUDENT);
        studentRole.setId(1L);
        studentRole.setUsers(new HashSet<>());

        tutorRole = new Role(RoleType.TUTOR);
        tutorRole.setId(2L);
        tutorRole.setUsers(new HashSet<>());

        staffRole = new Role(RoleType.STAFF);
        staffRole.setId(3L);
        staffRole.setUsers(new HashSet<>());

        UserRoleManager.addRoleToUser(user, studentRole);
    }

    @Test
    void testAddRoleToUser() {
        assertTrue(UserRoleManager.hasRole(user, RoleType.STUDENT));
        assertEquals(1, user.getRoles().size());
        assertEquals(1, studentRole.getUsers().size());

        boolean addResult = UserRoleManager.addRoleToUser(user, tutorRole);
        assertTrue(addResult);
        assertEquals(2, user.getRoles().size());
        assertTrue(UserRoleManager.hasRole(user, RoleType.TUTOR));
        assertEquals(1, tutorRole.getUsers().size());

        boolean duplicateResult = UserRoleManager.addRoleToUser(user, tutorRole);
        assertFalse(duplicateResult);
        assertEquals(2, user.getRoles().size());
    }

    @Test
    void testRemoveRoleFromUser() {
        UserRoleManager.addRoleToUser(user, tutorRole);
        assertEquals(2, user.getRoles().size());
        assertEquals(1, tutorRole.getUsers().size());

        boolean removeResult = UserRoleManager.removeRoleFromUser(user, studentRole);
        assertTrue(removeResult);
        assertEquals(1, user.getRoles().size());
        assertFalse(UserRoleManager.hasRole(user, RoleType.STUDENT));
        assertTrue(UserRoleManager.hasRole(user, RoleType.TUTOR));
        assertEquals(0, studentRole.getUsers().size());

        boolean removeAgainResult = UserRoleManager.removeRoleFromUser(user, studentRole);
        assertFalse(removeAgainResult);
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void testHasRole() {
        assertTrue(UserRoleManager.hasRole(user, RoleType.STUDENT));
        assertFalse(UserRoleManager.hasRole(user, RoleType.TUTOR));
        assertFalse(UserRoleManager.hasRole(user, RoleType.STAFF));

        UserRoleManager.addRoleToUser(user, staffRole);
        assertTrue(UserRoleManager.hasRole(user, RoleType.STAFF));
    }
}