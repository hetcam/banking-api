package org.example.bankingapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bankingapi.dto.AccountResponse;
import org.example.bankingapi.dto.CreateAccountRequest;
import org.example.bankingapi.dto.UpdateAccountRequest;
import org.example.bankingapi.entity.Account;
import org.example.bankingapi.exception.AccountNotFoundException;
import org.example.bankingapi.exception.DuplicateAccountException;
import org.example.bankingapi.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    private static final Long ACCOUNT_ID = 1L;
    private static final String ACCOUNT_NUMBER = "ACC001";
    private static final String ACCOUNT_HOLDER = "John Doe";
    private static final BigDecimal BALANCE = new BigDecimal("1000.00");
    private static final String CURRENCY = "USD";
    private static final Instant NOW = Instant.now();

    private AccountResponse accountResponse() {
        return AccountResponse.builder()
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
    @DisplayName("POST /api/accounts")
    class CreateAccount {

        @Test
        @DisplayName("returns 201 and created account")
        void createAccount_success() throws Exception {
            CreateAccountRequest request = CreateAccountRequest.builder()
                    .accountNumber(ACCOUNT_NUMBER)
                    .accountHolderName(ACCOUNT_HOLDER)
                    .balance(BALANCE)
                    .currency(CURRENCY)
                    .build();
            AccountResponse response = accountResponse();
            given(accountService.createAccount(any(CreateAccountRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER))
                    .andExpect(jsonPath("$.accountHolderName").value(ACCOUNT_HOLDER))
                    .andExpect(jsonPath("$.balance").value(1000.00))
                    .andExpect(jsonPath("$.currency").value(CURRENCY))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(accountService).createAccount(any(CreateAccountRequest.class));
        }

        @Test
        @DisplayName("returns 409 when account number already exists")
        void createAccount_duplicate_returnsConflict() throws Exception {
            CreateAccountRequest request = CreateAccountRequest.builder()
                    .accountNumber(ACCOUNT_NUMBER)
                    .accountHolderName(ACCOUNT_HOLDER)
                    .balance(BALANCE)
                    .currency(CURRENCY)
                    .build();
            willThrow(new DuplicateAccountException(ACCOUNT_NUMBER)).given(accountService).createAccount(any(CreateAccountRequest.class));

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(content().string(containsString("Account already exists with account number: " + ACCOUNT_NUMBER)));
        }

        @Test
        @DisplayName("returns 400 when request is invalid")
        void createAccount_invalidRequest_returnsBadRequest() throws Exception {
            String invalidBody = "{\"accountNumber\":\"\",\"accountHolderName\":\"\",\"balance\":-1,\"currency\":\"AB\"}";

            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/accounts")
    class GetAllAccounts {

        @Test
        @DisplayName("returns 200 and list of accounts")
        void getAllAccounts_success() throws Exception {
            List<AccountResponse> accounts = List.of(accountResponse());
            given(accountService.getAllAccounts()).willReturn(accounts);

            mockMvc.perform(get("/api/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$[0].accountNumber").value(ACCOUNT_NUMBER));

            verify(accountService).getAllAccounts();
        }

        @Test
        @DisplayName("returns 200 and empty array when no accounts")
        void getAllAccounts_empty() throws Exception {
            given(accountService.getAllAccounts()).willReturn(List.of());

            mockMvc.perform(get("/api/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/accounts/{id}")
    class GetAccountById {

        @Test
        @DisplayName("returns 200 and account when found")
        void getAccountById_success() throws Exception {
            AccountResponse response = accountResponse();
            given(accountService.getAccountById(ACCOUNT_ID)).willReturn(response);

            mockMvc.perform(get("/api/accounts/{id}", ACCOUNT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ACCOUNT_ID))
                    .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER))
                    .andExpect(jsonPath("$.accountHolderName").value(ACCOUNT_HOLDER));

            verify(accountService).getAccountById(ACCOUNT_ID);
        }

        @Test
        @DisplayName("returns 404 when account not found")
        void getAccountById_notFound() throws Exception {
            willThrow(new AccountNotFoundException(ACCOUNT_ID)).given(accountService).getAccountById(ACCOUNT_ID);

            mockMvc.perform(get("/api/accounts/{id}", ACCOUNT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Account not found with id: " + ACCOUNT_ID)));
        }
    }

    @Nested
    @DisplayName("PUT /api/accounts/{id}")
    class UpdateAccount {

        @Test
        @DisplayName("returns 200 and updated account")
        void updateAccount_success() throws Exception {
            UpdateAccountRequest request = UpdateAccountRequest.builder()
                    .accountHolderName("Jane Doe")
                    .balance(new BigDecimal("2000.00"))
                    .build();
            AccountResponse response = AccountResponse.builder()
                    .id(ACCOUNT_ID)
                    .accountNumber(ACCOUNT_NUMBER)
                    .accountHolderName("Jane Doe")
                    .balance(new BigDecimal("2000.00"))
                    .currency(CURRENCY)
                    .status(Account.AccountStatus.ACTIVE)
                    .createdAt(NOW)
                    .updatedAt(NOW)
                    .build();
            given(accountService.updateAccount(eq(ACCOUNT_ID), any(UpdateAccountRequest.class))).willReturn(response);

            mockMvc.perform(put("/api/accounts/{id}", ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountHolderName").value("Jane Doe"))
                    .andExpect(jsonPath("$.balance").value(2000.00));

            verify(accountService).updateAccount(eq(ACCOUNT_ID), any(UpdateAccountRequest.class));
        }

        @Test
        @DisplayName("returns 404 when account not found")
        void updateAccount_notFound() throws Exception {
            UpdateAccountRequest request = UpdateAccountRequest.builder()
                    .accountHolderName("Jane Doe")
                    .build();
            willThrow(new AccountNotFoundException(ACCOUNT_ID)).given(accountService).updateAccount(eq(ACCOUNT_ID), any(UpdateAccountRequest.class));

            mockMvc.perform(put("/api/accounts/{id}", ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Account not found with id: " + ACCOUNT_ID)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/accounts/{id}")
    class DeleteAccount {

        @Test
        @DisplayName("returns 204 when account deleted")
        void deleteAccount_success() throws Exception {
            mockMvc.perform(delete("/api/accounts/{id}", ACCOUNT_ID))
                    .andExpect(status().isNoContent());

            verify(accountService).deleteAccount(ACCOUNT_ID);
        }

        @Test
        @DisplayName("returns 404 when account not found")
        void deleteAccount_notFound() throws Exception {
            willThrow(new AccountNotFoundException(ACCOUNT_ID)).given(accountService).deleteAccount(ACCOUNT_ID);

            mockMvc.perform(delete("/api/accounts/{id}", ACCOUNT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Account not found with id: " + ACCOUNT_ID)));
        }
    }
}
