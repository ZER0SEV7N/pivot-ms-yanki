package com.pe.pivot.yanki.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;

/**
 * Request payload for topping up an unlinked Yanki wallet balance.
 */
@Builder
public record TopUpRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be at least 0.01") BigDecimal amount
) {
}