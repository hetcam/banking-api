package org.example.bankingapi.service;

import org.example.bankingapi.dto.AccountResponse;
import org.example.bankingapi.dto.CreateAccountRequest;
import org.example.bankingapi.dto.UpdateAccountRequest;
import org.example.bankingapi.entity.Account;
import org.example.bankingapi.exception.AccountNotFoundException;
import org.example.bankingapi.exception.DuplicateAccountException;
import org.example.bankingapi.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private static final Long ACCOUNT_ID = 1L;
    private static final String ACCOUNT_NUMBER = "ACC001";
    private static final String ACCOUNT_HOLDER = "John Doe";
    private static final BigDecimal BALANCE = new BigDecimal("1000.00");
    private static final String CURRENCY = "USD";
    private static final Instant NOW = Instant.now();

    private Account savedAccount;

    @BeforeEach
    void setUp() {
        savedAccount = Account.builder()
                .id(ACCOUNT_ID)
                .accountNumber(ACCOUNT_NUMBER)
                .accountHolderName(ACCOUNT_HOLDER)
                .balance(BALANCE)
                .currency(CURRENCY)
                .status(Account.AccountStatus.ACTIVE)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();
    }

    @Nested
    @DisplayName("createAccount")
    class CreateAccount {

        @Test
        @DisplayName("creates account and returns response")
        void createAccount_success() {
            CreateAccountRequest request = CreateAccountRequest.builder()
                    .accountNumber(ACCOUNT_NUMBER)
                    .accountHolderName(ACCOUNT_HOLDER)
                    .balance(BALANCE)
                    .currency(CURRENCY)
                    .build();

            given(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).willReturn(false);
            given(accountRepository.save(any(Account.class))).willReturn(savedAccount);

            AccountResponse response = accountService.createAccount(request);

            assertThat(response.getId()).isEqualTo(ACCOUNT_ID);
            assertThat(response.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
            assertThat(response.getAccountHolderName()).isEqualTo(ACCOUNT_HOLDER);
            assertThat(response.getBalance()).isEqualByComparingTo(BALANCE);
            assertThat(response.getCurrency()).isEqualTo(CURRENCY);
            assertThat(response.getStatus()).isEqualTo(Account.AccountStatus.ACTIVE);

            ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(captor.capture());
            Account saved = captor.getValue();
            assertThat(saved.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
            assertThat(saved.getAccountHolderName()).isEqualTo(ACCOUNT_HOLDER);
            assertThat(saved.getBalance()).isEqualByComparingTo(BALANCE);
            assertThat(saved.getCurrency()).isEqualTo(CURRENCY);
            assertThat(saved.getStatus()).isEqualTo(Account.AccountStatus.ACTIVE);
        }

        @Test
        @DisplayName("uses ZERO balance when balance is null")
        void createAccount_nullBalance_defaultsToZero() {
            CreateAccountRequest request = CreateAccountRequest.builder()
                    .accountNumber(ACCOUNT_NUMBER)
                    .accountHolderName(ACCOUNT_HOLDER)
                    .balance(null)
                    .currency(CURRENCY)
                    .build();

            Account accountWithZeroBalance = Account.builder()
                    .id(ACCOUNT_ID)
                    .accountNumber(ACCOUNT_NUMBER)
                    .accountHolderName(ACCOUNT_HOLDER)
                    .balance(BigDecimal.ZERO)
                    .currency(CURRENCY)
                    .status(Account.AccountStatus.ACTIVE)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .build();

            given(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).willReturn(false);
            given(accountRepository.save(any(Account.class))).willReturn(accountWithZeroBalance);

            AccountResponse response = accountService.createAccount(request);

            assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(captor.capture());
            assertThat(captor.getValue().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("throws DuplicateAccountException when account number exists")
        void createAccount_duplicateAccountNumber_throws() {
            CreateAccountRequest request = CreateAccountRequest.builder()
                    .accountNumber(ACCOUNT_NUMBER)
                    .accountHolderName(ACCOUNT_HOLDER)
                    .balance(BALANCE)
                    .currency(CURRENCY)
                    .build();

            given(accountRepository.existsByAccountNumber(ACCOUNT_NUMBER)).willReturn(true);

            assertThatThrownBy(() -> accountService.createAccount(request))
                    .isInstanceOf(DuplicateAccountException.class)
                    .hasMessageContaining("Account already exists with account number: " + ACCOUNT_NUMBER);
            verify(accountRepository, never()).save(any(Account.class));
        }
    }

    @Nested
    @DisplayName("updateAccount")
    class UpdateAccount {

        @Test
        @DisplayName("updates account and returns response")
        void updateAccount_success() {
            UpdateAccountRequest request = UpdateAccountRequest.builder()
                    .accountHolderName("Jane Doe")
                    .balance(new BigDecimal("2000.00"))
                    .build();

            Account updatedAccount = Account.builder()
                    .id(ACCOUNT_ID)
                    .accountNumber(ACCOUNT_NUMBER)
                    .accountHolderName("Jane Doe")
                    .balance(new BigDecimal("2000.00"))
                    .currency(CURRENCY)
                    .status(Account.AccountStatus.ACTIVE)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .build();

            given(accountRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(savedAccount));
            given(accountRepository.save(any(Account.class))).willReturn(updatedAccount);

            AccountResponse response = accountService.updateAccount(ACCOUNT_ID, request);

            assertThat(response.getAccountHolderName()).isEqualTo("Jane Doe");
            assertThat(response.getBalance()).isEqualByComparingTo("2000.00");
        }

        @Test
        @DisplayName("updates only provided fields")
        void updateAccount_partialUpdate() {
            UpdateAccountRequest request = UpdateAccountRequest.builder()
                    .accountHolderName("Jane Doe")
                    .build();

            given(accountRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(savedAccount));
            given(accountRepository.save(any(Account.class))).willAnswer(inv -> inv.getArgument(0));

            accountService.updateAccount(ACCOUNT_ID, request);

            ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(captor.capture());
            Account saved = captor.getValue();
            assertThat(saved.getAccountHolderName()).isEqualTo("Jane Doe");
            assertThat(saved.getBalance()).isEqualByComparingTo(BALANCE);
            assertThat(saved.getCurrency()).isEqualTo(CURRENCY);
        }

        @Test
        @DisplayName("throws AccountNotFoundException when account does not exist")
        void updateAccount_notFound_throws() {
            UpdateAccountRequest request = UpdateAccountRequest.builder()
                    .accountHolderName("Jane Doe")
                    .build();

            given(accountRepository.findById(ACCOUNT_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.updateAccount(ACCOUNT_ID, request))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("Account not found with id: " + ACCOUNT_ID);
            verify(accountRepository, never()).save(any(Account.class));
        }
    }

    @Nested
    @DisplayName("getAllAccounts")
    class GetAllAccounts {

        @Test
        @DisplayName("returns empty list when no accounts")
        void getAllAccounts_empty() {
            given(accountRepository.findAll()).willReturn(List.of());

            List<AccountResponse> result = accountService.getAllAccounts();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns all accounts as responses")
        void getAllAccounts_returnsList() {
            given(accountRepository.findAll()).willReturn(List.of(savedAccount));

            List<AccountResponse> result = accountService.getAllAccounts();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(ACCOUNT_ID);
            assertThat(result.get(0).getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
        }
    }

    @Nested
    @DisplayName("getAccountById")
    class GetAccountById {

        @Test
        @DisplayName("returns account when found")
        void getAccountById_success() {
            given(accountRepository.findById(ACCOUNT_ID)).willReturn(Optional.of(savedAccount));

            AccountResponse response = accountService.getAccountById(ACCOUNT_ID);

            assertThat(response.getId()).isEqualTo(ACCOUNT_ID);
            assertThat(response.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
            assertThat(response.getAccountHolderName()).isEqualTo(ACCOUNT_HOLDER);
        }

        @Test
        @DisplayName("throws AccountNotFoundException when not found")
        void getAccountById_notFound_throws() {
            given(accountRepository.findById(ACCOUNT_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getAccountById(ACCOUNT_ID))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("Account not found with id: " + ACCOUNT_ID);
        }
    }

    @Nested
    @DisplayName("deleteAccount")
    class DeleteAccount {

        @Test
        @DisplayName("deletes account when it exists")
        void deleteAccount_success() {
            given(accountRepository.existsById(ACCOUNT_ID)).willReturn(true);

            accountService.deleteAccount(ACCOUNT_ID);

            verify(accountRepository).deleteById(ACCOUNT_ID);
        }

        @Test
        @DisplayName("throws AccountNotFoundException when account does not exist")
        void deleteAccount_notFound_throws() {
            given(accountRepository.existsById(ACCOUNT_ID)).willReturn(false);

            assertThatThrownBy(() -> accountService.deleteAccount(ACCOUNT_ID))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("Account not found with id: " + ACCOUNT_ID);
            verify(accountRepository, never()).deleteById(any());
        }
    }
}
