package com.pe.pivot.yanki.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Core Domain Model representing a phone-to-phone Yanki payment.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YankiPayment {

  private String id;
  private String transactionId;
  private String senderPhoneNumber;
  private String receiverPhoneNumber;
  private String senderDebitCardId;
  private String receiverDebitCardId;
  private String currency;
  private BigDecimal amount;
  private String description;
  private YankiPaymentStatus status;
  private String failureReason;
  private Instant createdAt;
  private Instant updatedAt;

  // Convenience method to determine if the payment requires settlement with a bank account.
  public boolean requiresBankSettlement() {
    return (this.senderDebitCardId != null && !this.senderDebitCardId.isBlank())
            || (this.receiverDebitCardId != null && !this.receiverDebitCardId.isBlank());
  }
}