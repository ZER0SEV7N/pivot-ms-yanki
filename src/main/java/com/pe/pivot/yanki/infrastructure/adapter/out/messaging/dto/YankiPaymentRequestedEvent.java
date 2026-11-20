package com.pe.pivot.yanki.infrastructure.adapter.out.messaging.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;

/**
 * Kafka event published to yanki.payment.requested for primary debit card settlement in ms-account.
 */
@Builder
public record YankiPaymentRequestedEvent(
        String transactionId,
        String senderPhoneNumber,
        String receiverPhoneNumber,
        String senderDebitCardId,
        String receiverDebitCardId,
        String currency,
        BigDecimal amount,
        String description,
        Instant requestedAt
) {
}