package id.ac.ui.cs.advprog.udehnihauth.dto.response;

import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RoleResponseTest {

    @Test
    void testRoleResponseBuilder() {
        LocalDateTime now = LocalDateTime.now();

        RoleResponse response = RoleResponse.builder()
                .success(true)
                .message("Role added successfully")
                .userId(1L)
                .roleType(RoleType.TUTOR)
                .build();

        assertTrue(response.isSuccess());
        assertEquals("Role added successfully", response.getMessage());
        assertEquals(1L, response.getUserId());
        assertEquals(RoleType.TUTOR, response.getRoleType());
        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp().isAfter(now.minusSeconds(1)));
        assertTrue(response.getTimestamp().isBefore(now.plusSeconds(1)));
    }

    @Test
    void testRoleResponseWithDefaultTimestamp() {
        RoleResponse response = RoleResponse.builder()
                .success(false)
                .message("Failed to add role")
                .userId(2L)
                .roleType(RoleType.STAFF)
                .build();

        assertFalse(response.isSuccess());
        assertEquals("Failed to add role", response.getMessage());
        assertEquals(2L, response.getUserId());
        assertEquals(RoleType.STAFF, response.getRoleType());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testRoleResponseConstructors() {
        RoleResponse response1 = new RoleResponse();
        assertNull(response1.getMessage());
        assertNull(response1.getUserId());
        assertNull(response1.getRoleType());
        assertFalse(response1.isSuccess());
        assertNotNull(response1.getTimestamp());

        LocalDateTime timestamp = LocalDateTime.now();
        RoleResponse response2 = new RoleResponse(
                true,
                "Success message",
                1L,
                RoleType.TUTOR,
                timestamp
        );

        assertTrue(response2.isSuccess());
        assertEquals("Success message", response2.getMessage());
        assertEquals(1L, response2.getUserId());
        assertEquals(RoleType.TUTOR, response2.getRoleType());
        assertEquals(timestamp, response2.getTimestamp());
    }

    @Test
    void testRoleResponseSettersAndGetters() {
        RoleResponse response = new RoleResponse();
        LocalDateTime timestamp = LocalDateTime.now();

        response.setSuccess(true);
        response.setMessage("Test message");
        response.setUserId(3L);
        response.setRoleType(RoleType.STUDENT);
        response.setTimestamp(timestamp);

        assertTrue(response.isSuccess());
        assertEquals("Test message", response.getMessage());
        assertEquals(3L, response.getUserId());
        assertEquals(RoleType.STUDENT, response.getRoleType());
        assertEquals(timestamp, response.getTimestamp());
    }

    @Test
    void testRoleResponseEqualsAndHashCode() {
        LocalDateTime timestamp = LocalDateTime.now();

        RoleResponse response1 = RoleResponse.builder()
                .success(true)
                .message("Test")
                .userId(1L)
                .roleType(RoleType.TUTOR)
                .timestamp(timestamp)
                .build();

        RoleResponse response2 = RoleResponse.builder()
                .success(true)
                .message("Test")
                .userId(1L)
                .roleType(RoleType.TUTOR)
                .timestamp(timestamp)
                .build();

        RoleResponse response3 = RoleResponse.builder()
                .success(false)
                .message("Different")
                .userId(2L)
                .roleType(RoleType.STAFF)
                .timestamp(timestamp)
                .build();

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotEquals(response1, response3);
        assertNotEquals(response1.hashCode(), response3.hashCode());
    }

    @Test
    void testRoleResponseToString() {
        RoleResponse response = RoleResponse.builder()
                .success(true)
                .message("Role operation completed")
                .userId(1L)
                .roleType(RoleType.TUTOR)
                .build();

        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("success"));
        assertTrue(toString.contains("message"));
        assertTrue(toString.contains("userId"));
        assertTrue(toString.contains("roleType"));
        assertTrue(toString.contains("timestamp"));
        assertTrue(toString.contains("true"));
        assertTrue(toString.contains("TUTOR"));
    }

    @Test
    void testRoleResponseWithDifferentRoleTypes() {
        RoleResponse studentResponse = RoleResponse.builder()
                .success(true)
                .message("Student role added")
                .userId(1L)
                .roleType(RoleType.STUDENT)
                .build();

        RoleResponse tutorResponse = RoleResponse.builder()
                .success(true)
                .message("Tutor role added")
                .userId(1L)
                .roleType(RoleType.TUTOR)
                .build();

        RoleResponse staffResponse = RoleResponse.builder()
                .success(true)
                .message("Staff role added")
                .userId(1L)
                .roleType(RoleType.STAFF)
                .build();

        assertEquals(RoleType.STUDENT, studentResponse.getRoleType());
        assertEquals(RoleType.TUTOR, tutorResponse.getRoleType());
        assertEquals(RoleType.STAFF, staffResponse.getRoleType());

        assertNotEquals(studentResponse.getRoleType(), tutorResponse.getRoleType());
        assertNotEquals(tutorResponse.getRoleType(), staffResponse.getRoleType());
    }

    @Test
    void testRoleResponseTimestampBehavior() {
        RoleResponse response1 = RoleResponse.builder()
                .success(true)
                .message("Test")
                .userId(1L)
                .roleType(RoleType.TUTOR)
                .build();

        assertNotNull(response1.getTimestamp());

        LocalDateTime customTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        RoleResponse response2 = RoleResponse.builder()
                .success(true)
                .message("Test")
                .userId(1L)
                .roleType(RoleType.TUTOR)
                .timestamp(customTime)
                .build();

        assertEquals(customTime, response2.getTimestamp());
    }
}