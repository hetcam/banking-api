package org.example.bankingapi.exception;

public class DuplicateAccountException extends RuntimeException {

    public DuplicateAccountException(String accountNumber) {
        super("Account already exists with account number: " + accountNumber);
    }
}
