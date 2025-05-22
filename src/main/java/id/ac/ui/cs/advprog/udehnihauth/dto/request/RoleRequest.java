package id.ac.ui.cs.advprog.udehnihauth.dto.request;

import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    @NotNull(message = "Role type is required")
    private RoleType roleType;
}