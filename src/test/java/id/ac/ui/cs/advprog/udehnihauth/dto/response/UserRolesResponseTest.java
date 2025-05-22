package id.ac.ui.cs.advprog.udehnihauth.dto.response;

import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserRolesResponseTest {

    @Test
    void testUserRolesResponseBuilder() {
        Set<RoleType> roles = Set.of(RoleType.STUDENT, RoleType.TUTOR);

        UserRolesResponse response = UserRolesResponse.builder()
                .userId(1L)
                .roles(roles)
                .isStudent(true)
                .isTutor(true)
                .isStaff(false)
                .build();

        assertEquals(1L, response.getUserId());
        assertEquals(roles, response.getRoles());
        assertTrue(response.isStudent());
        assertTrue(response.isTutor());
        assertFalse(response.isStaff());
    }

    @Test
    void testUserRolesResponseWithEmptyRoles() {
        Set<RoleType> emptyRoles = new HashSet<>();

        UserRolesResponse response = UserRolesResponse.builder()
                .userId(2L)
                .roles(emptyRoles)
                .isStudent(false)
                .isTutor(false)
                .isStaff(false)
                .build();

        assertEquals(2L, response.getUserId());
        assertTrue(response.getRoles().isEmpty());
        assertFalse(response.isStudent());
        assertFalse(response.isTutor());
        assertFalse(response.isStaff());
    }

    @Test
    void testUserRolesResponseWithAllRoles() {
        Set<RoleType> allRoles = Set.of(RoleType.STUDENT, RoleType.TUTOR, RoleType.STAFF);

        UserRolesResponse response = UserRolesResponse.builder()
                .userId(3L)
                .roles(allRoles)
                .isStudent(true)
                .isTutor(true)
                .isStaff(true)
                .build();

        assertEquals(3L, response.getUserId());
        assertEquals(3, response.getRoles().size());
        assertTrue(response.getRoles().contains(RoleType.STUDENT));
        assertTrue(response.getRoles().contains(RoleType.TUTOR));
        assertTrue(response.getRoles().contains(RoleType.STAFF));
        assertTrue(response.isStudent());
        assertTrue(response.isTutor());
        assertTrue(response.isStaff());
    }

    @Test
    void testUserRolesResponseConstructors() {
        UserRolesResponse response1 = new UserRolesResponse();
        assertNull(response1.getUserId());
        assertNull(response1.getRoles());
        assertFalse(response1.isStudent());
        assertFalse(response1.isTutor());
        assertFalse(response1.isStaff());

        Set<RoleType> roles = Set.of(RoleType.STAFF);
        UserRolesResponse response2 = new UserRolesResponse(
                4L,
                roles,
                false,
                false,
                true
        );

        assertEquals(4L, response2.getUserId());
        assertEquals(roles, response2.getRoles());
        assertFalse(response2.isStudent());
        assertFalse(response2.isTutor());
        assertTrue(response2.isStaff());
    }

    @Test
    void testUserRolesResponseSettersAndGetters() {
        UserRolesResponse response = new UserRolesResponse();
        Set<RoleType> roles = Set.of(RoleType.TUTOR);

        response.setUserId(5L);
        response.setRoles(roles);
        response.setStudent(false);
        response.setTutor(true);
        response.setStaff(false);

        assertEquals(5L, response.getUserId());
        assertEquals(roles, response.getRoles());
        assertFalse(response.isStudent());
        assertTrue(response.isTutor());
        assertFalse(response.isStaff());
    }

    @Test
    void testUserRolesResponseEqualsAndHashCode() {
        Set<RoleType> roles = Set.of(RoleType.STUDENT);

        UserRolesResponse response1 = UserRolesResponse.builder()
                .userId(1L)
                .roles(roles)
                .isStudent(true)
                .isTutor(false)
                .isStaff(false)
                .build();

        UserRolesResponse response2 = UserRolesResponse.builder()
                .userId(1L)
                .roles(roles)
                .isStudent(true)
                .isTutor(false)
                .isStaff(false)
                .build();

        UserRolesResponse response3 = UserRolesResponse.builder()
                .userId(2L)
                .roles(Set.of(RoleType.STAFF))
                .isStudent(false)
                .isTutor(false)
                .isStaff(true)
                .build();

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1, response3);
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    void testUserRolesResponseToString() {
        Set<RoleType> roles = Set.of(RoleType.STUDENT, RoleType.TUTOR);

        UserRolesResponse response = UserRolesResponse.builder()
                .userId(1L)
                .roles(roles)
                .isStudent(true)
                .isTutor(true)
                .isStaff(false)
                .build();

        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("userId"));
        assertTrue(toString.contains("roles"));
        assertTrue(toString.contains("isStudent"));
        assertTrue(toString.contains("isTutor"));
        assertTrue(toString.contains("isStaff"));
        assertTrue(toString.contains("1"));
    }

    @Test
    void testUserRolesResponseBooleanFlags() {
        UserRolesResponse studentOnly = UserRolesResponse.builder()
                .userId(1L)
                .roles(Set.of(RoleType.STUDENT))
                .isStudent(true)
                .isTutor(false)
                .isStaff(false)
                .build();

        assertTrue(studentOnly.isStudent());
        assertFalse(studentOnly.isTutor());
        assertFalse(studentOnly.isStaff());

        UserRolesResponse tutorOnly = UserRolesResponse.builder()
                .userId(2L)
                .roles(Set.of(RoleType.TUTOR))
                .isStudent(false)
                .isTutor(true)
                .isStaff(false)
                .build();

        assertFalse(tutorOnly.isStudent());
        assertTrue(tutorOnly.isTutor());
        assertFalse(tutorOnly.isStaff());

        UserRolesResponse staffOnly = UserRolesResponse.builder()
                .userId(3L)
                .roles(Set.of(RoleType.STAFF))
                .isStudent(false)
                .isTutor(false)
                .isStaff(true)
                .build();

        assertFalse(staffOnly.isStudent());
        assertFalse(staffOnly.isTutor());
        assertTrue(staffOnly.isStaff());
    }

    @Test
    void testUserRolesResponseWithNullRoles() {
        UserRolesResponse response = UserRolesResponse.builder()
                .userId(1L)
                .roles(null)
                .isStudent(false)
                .isTutor(false)
                .isStaff(false)
                .build();

        assertEquals(1L, response.getUserId());
        assertNull(response.getRoles());
        assertFalse(response.isStudent());
        assertFalse(response.isTutor());
        assertFalse(response.isStaff());
    }
}