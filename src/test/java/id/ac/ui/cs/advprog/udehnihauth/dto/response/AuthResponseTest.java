package id.ac.ui.cs.advprog.udehnihauth.dto.response;

import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void testAuthResponseDTO() {
        String token = "jwt-token";
        String email = "test@example.com";
        String name = "Test User";
        Set<RoleType> roles = new HashSet<>();
        roles.add(RoleType.STUDENT);

        AuthResponse dto = AuthResponse.builder()
                .token(token)
                .email(email)
                .name(name)
                .roles(roles)
                .build();

        assertEquals(token, dto.getToken());
        assertEquals(email, dto.getEmail());
        assertEquals(name, dto.getName());
        assertEquals(roles, dto.getRoles());
        assertTrue(dto.getRoles().contains(RoleType.STUDENT));
    }

    @Test
    void testEmptyRoles() {
        AuthResponse dto = AuthResponse.builder()
                .token("token")
                .email("email")
                .name("name")
                .roles(new HashSet<>())
                .build();

        assertNotNull(dto.getRoles());
        assertTrue(dto.getRoles().isEmpty());
    }
}
