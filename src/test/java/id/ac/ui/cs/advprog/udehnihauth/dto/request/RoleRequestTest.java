package id.ac.ui.cs.advprog.udehnihauth.dto.request;

import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoleRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRoleRequest() {
        RoleRequest request = RoleRequest.builder()
                .userId(1L)
                .roleType(RoleType.TUTOR)
                .build();

        Set<ConstraintViolation<RoleRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
        assertEquals(1L, request.getUserId());
        assertEquals(RoleType.TUTOR, request.getRoleType());
    }

    @Test
    void testRoleRequestWithNullUserId() {
        RoleRequest request = RoleRequest.builder()
                .userId(null)
                .roleType(RoleType.TUTOR)
                .build();

        Set<ConstraintViolation<RoleRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("User ID is required", violations.iterator().next().getMessage());
    }

    @Test
    void testRoleRequestWithNullRoleType() {
        RoleRequest request = RoleRequest.builder()
                .userId(1L)
                .roleType(null)
                .build();

        Set<ConstraintViolation<RoleRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Role type is required", violations.iterator().next().getMessage());
    }

    @Test
    void testRoleRequestWithAllNullValues() {
        RoleRequest request = RoleRequest.builder()
                .userId(null)
                .roleType(null)
                .build();

        Set<ConstraintViolation<RoleRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
    }

    @Test
    void testRoleRequestConstructors() {
        RoleRequest request1 = new RoleRequest();
        assertNull(request1.getUserId());
        assertNull(request1.getRoleType());

        RoleRequest request2 = new RoleRequest(1L, RoleType.STAFF);
        assertEquals(1L, request2.getUserId());
        assertEquals(RoleType.STAFF, request2.getRoleType());

        RoleRequest request3 = RoleRequest.builder()
                .userId(2L)
                .roleType(RoleType.STUDENT)
                .build();
        assertEquals(2L, request3.getUserId());
        assertEquals(RoleType.STUDENT, request3.getRoleType());
    }

    @Test
    void testRoleRequestSettersAndGetters() {
        RoleRequest request = new RoleRequest();

        request.setUserId(5L);
        request.setRoleType(RoleType.TUTOR);

        assertEquals(5L, request.getUserId());
        assertEquals(RoleType.TUTOR, request.getRoleType());
    }

    @Test
    void testRoleRequestEqualsAndHashCode() {
        RoleRequest request1 = RoleRequest.builder()
                .userId(1L)
                .roleType(RoleType.TUTOR)
                .build();

        RoleRequest request2 = RoleRequest.builder()
                .userId(1L)
                .roleType(RoleType.TUTOR)
                .build();

        RoleRequest request3 = RoleRequest.builder()
                .userId(2L)
                .roleType(RoleType.STAFF)
                .build();

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1, request3);
        assertNotEquals(request1.hashCode(), request3.hashCode());
    }

    @Test
    void testRoleRequestToString() {
        RoleRequest request = RoleRequest.builder()
                .userId(1L)
                .roleType(RoleType.TUTOR)
                .build();

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("userId"));
        assertTrue(toString.contains("roleType"));
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("TUTOR"));
    }
}