package org.example.bankingapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 3, max = 100)
    private String username;

    @Email(message = "Email must be valid")
    @Size(max = 255)
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters if provided")
    private String password;

    private Boolean enabled;

    private Set<String> roleNames;
}
