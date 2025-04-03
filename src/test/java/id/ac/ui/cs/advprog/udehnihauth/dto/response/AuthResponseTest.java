package id.ac.ui.cs.advprog.udehnihauth.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void testAuthResponseCreation() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .build();

        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(refreshToken, response.getRefreshToken());
    }

    @Test
    void testNoArgsConstructor() {
        AuthResponse response = new AuthResponse();

        assertNotNull(response);
        assertNull(response.getToken());
        assertNull(response.getRefreshToken());
    }

    @Test
    void testSetters() {
        AuthResponse response = new AuthResponse();
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        response.setToken(token);
        response.setRefreshToken(refreshToken);

        assertEquals(token, response.getToken());
        assertEquals(refreshToken, response.getRefreshToken());
    }

    @Test
    void testEquals() {
        AuthResponse response1 = AuthResponse.builder()
                .token("token1")
                .refreshToken("refreshToken1")
                .build();

        AuthResponse response2 = AuthResponse.builder()
                .token("token1")
                .refreshToken("refreshToken1")
                .build();

        AuthResponse response3 = AuthResponse.builder()
                .token("token2")
                .refreshToken("refreshToken1")
                .build();

        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
    }

    @Test
    void testHashCode() {
        AuthResponse response1 = AuthResponse.builder()
                .token("token1")
                .refreshToken("refreshToken1")
                .build();

        AuthResponse response2 = AuthResponse.builder()
                .token("token1")
                .refreshToken("refreshToken1")
                .build();

        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString() {
        String token = "token123";
        String refreshToken = "refresh456";
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .build();

        String toString = response.toString();

        assertTrue(toString.contains(token));
        assertTrue(toString.contains(refreshToken));
    }
}
