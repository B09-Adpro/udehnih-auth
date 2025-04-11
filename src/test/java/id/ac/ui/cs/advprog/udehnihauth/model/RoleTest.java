package id.ac.ui.cs.advprog.udehnihauth.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1L);
        role.setName(RoleType.STUDENT);
    }

    @Test
    void testRoleProperties() {
        assertEquals(1L, role.getId());
        assertEquals(RoleType.STUDENT, role.getName());
        assertNotNull(role.getUsers());
        assertTrue(role.getUsers().isEmpty());
    }

    @Test
    void testRoleConstructor() {
        Role roleWithName = new Role(RoleType.TUTOR);
        assertEquals(RoleType.TUTOR, roleWithName.getName());
        assertNotNull(roleWithName.getUsers());
        assertTrue(roleWithName.getUsers().isEmpty());
    }
}