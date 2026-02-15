package org.example.bankingapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "Account number is required")
    @Size(min = 1, max = 50)
    private String accountNumber;

    @NotBlank(message = "Account holder name is required")
    @Size(min = 1, max = 255)
    private String accountHolderName;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0", message = "Balance must be non-negative")
    private BigDecimal balance;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3)
    private String currency;
}
