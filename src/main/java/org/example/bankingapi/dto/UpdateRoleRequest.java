package org.example.bankingapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {

    @Size(min = 1, max = 50)
    private String name;

    @Size(max = 255)
    private String description;

    /** Permission names (e.g. ACCOUNTS_READ, USERS_WRITE). When set, replaces role's permissions. */
    private Set<String> permissionNames;
}
