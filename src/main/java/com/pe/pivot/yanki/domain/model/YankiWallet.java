package com.pe.pivot.yanki.domain.model;

import com.pe.pivot.yanki.domain.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Core Domain Model representing a Yanki mobile wallet.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YankiWallet {

  private String id;
  private YankiDocumentType documentType;
  private String documentNumber;
  private String phoneNumber;
  private String imei;
  private String email;
  private BigDecimal balance;
  private String linkedDebitCardId;
  private YankiWalletStatus status;
  private Instant createdAt;
  private Instant updatedAt;

  // Convenience methods for business logic
  public boolean isActive() {
    return this.status == YankiWalletStatus.ACTIVE;
  }

  // Convenience method to check if the wallet has a linked debit card
  public boolean hasLinkedCard() {
    return this.linkedDebitCardId != null && !this.linkedDebitCardId.isBlank();
  }

  /**
   * Debits the specified amount from the internal wallet balance.
   *
   * @param amount positive amount to debit
   */
  public void debit(BigDecimal amount) {
    BigDecimal current = Optional.ofNullable(this.balance).orElse(BigDecimal.ZERO);
    if (current.compareTo(amount) < 0) {
      throw new BusinessRuleException(
              "Insufficient Yanki wallet balance. Available: " + current);
    }
    this.balance = current.subtract(amount);
    this.updatedAt = Instant.now();
  }

  /**
   * Credits the specified amount to the internal wallet balance.
   *
   * @param amount positive amount to credit
   */
  public void credit(BigDecimal amount) {
    BigDecimal current = Optional.ofNullable(this.balance).orElse(BigDecimal.ZERO);
    this.balance = current.add(amount);
    this.updatedAt = Instant.now();
  }
}
