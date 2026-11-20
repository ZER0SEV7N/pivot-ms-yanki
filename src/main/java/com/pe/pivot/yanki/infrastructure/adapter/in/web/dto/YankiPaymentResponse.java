package com.pe.pivot.yanki.infrastructure.adapter.in.web.dto;

import com.pe.pivot.yanki.domain.model.YankiPaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;

/**
 * Response payload representing a Yanki payment status and metadata.
 */
@Builder
public record YankiPaymentResponse(
        String id,
        String transactionId,
        String senderPhoneNumber,
        String receiverPhoneNumber,
        String currency,
        BigDecimal amount,
        String description,
        YankiPaymentStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
}