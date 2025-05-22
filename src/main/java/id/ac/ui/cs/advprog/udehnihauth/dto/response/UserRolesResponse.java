package id.ac.ui.cs.advprog.udehnihauth.dto.response;

import id.ac.ui.cs.advprog.udehnihauth.model.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRolesResponse {
    private Long userId;
    private Set<RoleType> roles;
    private boolean isStudent;
    private boolean isTutor;
    private boolean isStaff;
}