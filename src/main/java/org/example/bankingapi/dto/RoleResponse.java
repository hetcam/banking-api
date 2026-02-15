package org.example.bankingapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bankingapi.entity.Role;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private Long id;
    private String name;
    private String description;
    private Set<String> permissionNames;

    public static RoleResponse fromEntity(Role role) {
        Set<String> permissionNames = role.getPermissions().stream()
                .map(p -> p.getName())
                .collect(Collectors.toSet());
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissionNames(permissionNames)
                .build();
    }
}
