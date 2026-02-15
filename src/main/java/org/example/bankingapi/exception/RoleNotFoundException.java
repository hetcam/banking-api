package org.example.bankingapi.exception;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(Long id) {
        super("Role not found with id: " + id);
    }

    public RoleNotFoundException(String name) {
        super("Role not found: " + name);
    }
}
