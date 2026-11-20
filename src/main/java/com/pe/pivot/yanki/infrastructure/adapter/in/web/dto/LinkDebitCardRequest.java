package com.pe.pivot.yanki.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Request payload for linking a bank DebitCard ID to a Yanki wallet.
 */
@Builder
public record LinkDebitCardRequest(
        @NotBlank(message = "debitCardId is required") String debitCardId
) {
}