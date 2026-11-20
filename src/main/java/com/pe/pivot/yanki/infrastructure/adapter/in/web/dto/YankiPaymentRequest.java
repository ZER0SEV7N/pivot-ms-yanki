package com.pe.pivot.yanki.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;

/**
 * Request payload for sending a phone-to-phone Yanki payment.
 */
@Builder
public record YankiPaymentRequest(
        @NotBlank(message = "senderPhoneNumber is required") String senderPhoneNumber,
        @NotBlank(message = "receiverPhoneNumber is required") String receiverPhoneNumber,
        String currency,
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be at least 0.01") BigDecimal amount,
        String description
) {
}