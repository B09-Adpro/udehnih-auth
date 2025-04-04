package id.ac.ui.cs.advprog.udehnihauth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    @NotBlank(message = "Access token is required")
    private String accessToken;

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}