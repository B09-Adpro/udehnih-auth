package id.ac.ui.cs.advprog.udehnihauth.dto.request;

import id.ac.ui.cs.advprog.udehnihauth.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String email;
    private String fullName;
    private String password;
    private String phoneNumber;
    private LocalDateTime dateOfBirth;
    private Gender gender;
}
