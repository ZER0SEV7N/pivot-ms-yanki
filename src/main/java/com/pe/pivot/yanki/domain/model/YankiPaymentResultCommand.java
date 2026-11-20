package com.pe.pivot.yanki.domain.model;

import java.time.Instant;
import lombok.Builder;

/**
 * Domain command carrying the asynchronous settlement result returned by ms-account via Kafka.
 */
@Builder
public record YankiPaymentResultCommand(
        String transactionId,
        String status,
        String failureReason,
        Instant processedAt
) {

  // Convenience method to check if the payment status is COMPLETED.
  private static final String STATUS_COMPLETED = "COMPLETED";

  public boolean isCompleted() {
    return STATUS_COMPLETED.equalsIgnoreCase(this.status);
  }
}