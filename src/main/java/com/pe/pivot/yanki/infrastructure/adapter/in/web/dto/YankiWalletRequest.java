package com.pe.pivot.yanki.infrastructure.adapter.in.web.dto;

import com.pe.pivot.yanki.domain.model.YankiDocumentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;

/**
 * Request payload for registering a new Yanki mobile wallet.
 */
@Builder
public record YankiWalletRequest(
        @NotNull(message = "documentType is required") YankiDocumentType documentType,
        @NotBlank(message = "documentNumber is required") String documentNumber,
        @NotBlank(message = "phoneNumber is required") String phoneNumber,
        @NotBlank(message = "imei is required") String imei,
        @NotBlank(message = "email is required")
        @Email(message = "email must have a valid format") String email,
        @DecimalMin(value = "0.00", message = "initialBalance cannot be negative")
        BigDecimal initialBalance
) {
}