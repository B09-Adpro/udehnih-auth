package id.ac.ui.cs.advprog.udehnihauth.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRegisterRequestDTO() {
        RegisterRequest dto = RegisterRequest.builder()
                .email("test@example.com")
                .name("Test User")
                .password("password123")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidEmail() {
        RegisterRequest dto = RegisterRequest.builder()
                .email("invalid-email")
                .name("Test User")
                .password("password123")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Email should be valid", violations.iterator().next().getMessage());
    }

    @Test
    void testBlankFields() {
        RegisterRequest dto = RegisterRequest.builder()
                .email("")
                .name("")
                .password("")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals(4, violations.size());
    }
}
