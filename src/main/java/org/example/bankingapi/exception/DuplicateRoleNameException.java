package org.example.bankingapi.exception;

public class DuplicateRoleNameException extends RuntimeException {

    public DuplicateRoleNameException(String name) {
        super("Role already exists with name: " + name);
    }
}
