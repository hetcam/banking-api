package org.example.bankingapi.service;

import org.example.bankingapi.dto.CreateRoleRequest;
import org.example.bankingapi.dto.RoleResponse;
import org.example.bankingapi.dto.UpdateRoleRequest;
import org.example.bankingapi.entity.Permission;
import org.example.bankingapi.entity.Role;
import org.example.bankingapi.exception.DuplicateRoleNameException;
import org.example.bankingapi.exception.RoleNotFoundException;
import org.example.bankingapi.repository.PermissionRepository;
import org.example.bankingapi.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateRoleNameException(request.getName());
        }
        Set<Permission> permissions = resolvePermissions(request.getPermissionNames());
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .permissions(permissions)
                .build();
        role = roleRepository.save(role);
        return RoleResponse.fromEntity(role);
    }

    @Transactional
    public RoleResponse updateRole(Long id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));

        if (request.getName() != null && !request.getName().equals(role.getName())) {
            if (roleRepository.existsByName(request.getName())) {
                throw new DuplicateRoleNameException(request.getName());
            }
            role.setName(request.getName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getPermissionNames() != null) {
            role.setPermissions(resolvePermissions(request.getPermissionNames()));
        }

        role = roleRepository.save(role);
        return RoleResponse.fromEntity(role);
    }

    private Set<Permission> resolvePermissions(Set<String> permissionNames) {
        Set<Permission> permissions = new HashSet<>();
        if (permissionNames != null) {
            for (String name : permissionNames) {
                permissionRepository.findByName(name).ifPresent(permissions::add);
            }
        }
        return permissions;
    }

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        return RoleResponse.fromEntity(role);
    }

    @Transactional
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new RoleNotFoundException(id);
        }
        roleRepository.deleteById(id);
    }
}
