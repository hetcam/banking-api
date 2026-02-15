package org.example.bankingapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.example.bankingapi.entity.Account;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {

    @Size(min = 1, max = 255)
    private String accountHolderName;

    @DecimalMin(value = "0", message = "Balance must be non-negative")
    private BigDecimal balance;

    @Size(min = 3, max = 3)
    private String currency;

    private Account.AccountStatus status;
}
