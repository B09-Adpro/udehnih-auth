package id.ac.ui.cs.advprog.udehnihauth.dto.response;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenRefreshResponseTest {

    @Test
    void testTokenRefreshResponse() {
        String accessToken = "new-access-token";
        String refreshToken = "refresh-token";

        TokenRefreshResponse response = TokenRefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
    }
}