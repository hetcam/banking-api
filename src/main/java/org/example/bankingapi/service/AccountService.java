package org.example.bankingapi.service;

import org.example.bankingapi.dto.AccountResponse;
import org.example.bankingapi.dto.CreateAccountRequest;
import org.example.bankingapi.dto.UpdateAccountRequest;
import org.example.bankingapi.entity.Account;
import org.example.bankingapi.exception.AccountNotFoundException;
import org.example.bankingapi.exception.DuplicateAccountException;
import org.example.bankingapi.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new DuplicateAccountException(request.getAccountNumber());
        }
        Account account = Account.builder()
                .accountNumber(request.getAccountNumber())
                .accountHolderName(request.getAccountHolderName())
                .balance(request.getBalance() != null ? request.getBalance() : java.math.BigDecimal.ZERO)
                .currency(request.getCurrency())
                .status(Account.AccountStatus.ACTIVE)
                .build();
        account = accountRepository.save(account);
        return AccountResponse.fromEntity(account);
    }

    @Transactional
    public AccountResponse updateAccount(Long id, UpdateAccountRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (request.getAccountHolderName() != null) {
            account.setAccountHolderName(request.getAccountHolderName());
        }
        if (request.getBalance() != null) {
            account.setBalance(request.getBalance());
        }
        if (request.getCurrency() != null) {
            account.setCurrency(request.getCurrency());
        }
        if (request.getStatus() != null) {
            account.setStatus(request.getStatus());
        }

        account = accountRepository.save(account);
        return AccountResponse.fromEntity(account);
    }

    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        return AccountResponse.fromEntity(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new AccountNotFoundException(id);
        }
        accountRepository.deleteById(id);
    }
}
