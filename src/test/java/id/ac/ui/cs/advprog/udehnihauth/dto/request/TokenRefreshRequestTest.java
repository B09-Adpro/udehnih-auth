package id.ac.ui.cs.advprog.udehnihauth.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TokenRefreshRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidTokenRefreshRequest() {
        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        Set<ConstraintViolation<TokenRefreshRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testBlankRefreshToken() {
        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken("")
                .build();

        Set<ConstraintViolation<TokenRefreshRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Refresh token is required", violations.iterator().next().getMessage());
    }
}