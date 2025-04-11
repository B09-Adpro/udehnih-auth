package id.ac.ui.cs.advprog.udehnihauth.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidLoginRequestDTO() {
        LoginRequest dto = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidEmail() {
        LoginRequest dto = LoginRequest.builder()
                .email("invalid-email")
                .password("password123")
                .build();

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Email should be valid", violations.iterator().next().getMessage());
    }

    @Test
    void testBlankFields() {
        LoginRequest dto = LoginRequest.builder()
                .email("")
                .password("")
                .build();

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
    }
}
