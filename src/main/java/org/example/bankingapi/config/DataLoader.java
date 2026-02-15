package org.example.bankingapi.config;

import org.example.bankingapi.entity.Permission;
import org.example.bankingapi.entity.Role;
import org.example.bankingapi.repository.PermissionRepository;
import org.example.bankingapi.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Order(1)
public class DataLoader implements CommandLineRunner {

    public static final String PERMISSION_ACCOUNTS_READ = "ACCOUNTS_READ";
    public static final String PERMISSION_ACCOUNTS_WRITE = "ACCOUNTS_WRITE";
    public static final String PERMISSION_USERS_READ = "USERS_READ";
    public static final String PERMISSION_USERS_WRITE = "USERS_WRITE";
    public static final String PERMISSION_ROLES_READ = "ROLES_READ";
    public static final String PERMISSION_ROLES_WRITE = "ROLES_WRITE";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_OPERATOR = "OPERATOR";

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public DataLoader(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        // Create permissions
        Permission accountsRead = getOrCreatePermission(PERMISSION_ACCOUNTS_READ, "Read bank accounts");
        Permission accountsWrite = getOrCreatePermission(PERMISSION_ACCOUNTS_WRITE, "Create, update, delete accounts");
        Permission usersRead = getOrCreatePermission(PERMISSION_USERS_READ, "Read users");
        Permission usersWrite = getOrCreatePermission(PERMISSION_USERS_WRITE, "Create, update, delete users");
        Permission rolesRead = getOrCreatePermission(PERMISSION_ROLES_READ, "Read roles");
        Permission rolesWrite = getOrCreatePermission(PERMISSION_ROLES_WRITE, "Create, update, delete roles");

        // ADMIN: all permissions
        Set<Permission> adminPermissions = new HashSet<>();
        adminPermissions.add(accountsRead);
        adminPermissions.add(accountsWrite);
        adminPermissions.add(usersRead);
        adminPermissions.add(usersWrite);
        adminPermissions.add(rolesRead);
        adminPermissions.add(rolesWrite);
        getOrCreateRole(ROLE_ADMIN, "Administrator with full access", adminPermissions);

        // CUSTOMER: read accounts only
        Set<Permission> customerPermissions = new HashSet<>();
        customerPermissions.add(accountsRead);
        getOrCreateRole(ROLE_CUSTOMER, "Bank customer", customerPermissions);

        // OPERATOR: accounts read/write, users/roles read only
        Set<Permission> operatorPermissions = new HashSet<>();
        operatorPermissions.add(accountsRead);
        operatorPermissions.add(accountsWrite);
        operatorPermissions.add(usersRead);
        operatorPermissions.add(rolesRead);
        getOrCreateRole(ROLE_OPERATOR, "Back-office operator", operatorPermissions);
    }

    private Permission getOrCreatePermission(String name, String description) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder().name(name).description(description).build()));
    }

    private Role getOrCreateRole(String name, String description, Set<Permission> permissions) {
        return roleRepository.findByName(name)
                .map(existing -> {
                    if (existing.getPermissions().size() != permissions.size()) {
                        existing.setPermissions(permissions);
                        return roleRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(name).description(description).permissions(permissions).build()));
    }
}
