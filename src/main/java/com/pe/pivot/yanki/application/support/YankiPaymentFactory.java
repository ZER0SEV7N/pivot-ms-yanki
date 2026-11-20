package com.pe.pivot.yanki.application.support;

import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.model.YankiPaymentStatus;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Factory creating initialized YankiPayment domain objects with unique transaction IDs (SRP).
 */
@Component
public class YankiPaymentFactory {

  // Date format used to generate unique transaction IDs.
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final String DEFAULT_CURRENCY = "PEN";

  /**
   * Creates a new YankiPayment domain object, initializing its fields based on the provided
   *
   * @param incoming - the incoming YankiPayment containing amount, currency, and description
   * @param sender - the YankiWallet of the sender, providing phone number and linked debit card ID
   * @param receiver - the YankiWallet of the receiver, providing phone number and linked debit card ID
   * @param status - the initial status of the payment (e.g., PENDING, COMPLETED, FAILED)
   * @return - a fully constructed YankiPayment object with a unique transaction ID and timestamps
   */
  public YankiPayment create(
          YankiPayment incoming, YankiWallet sender, YankiWallet receiver, YankiPaymentStatus status) {
    Instant now = Instant.now();
    String datePart = LocalDate.now(ZoneOffset.UTC).format(DATE_FORMAT);
    String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    // Build and return a new YankiPayment object with all required fields initialized
    return YankiPayment.builder()
            .transactionId("YK-" + datePart + "-" + randomPart)
            .senderPhoneNumber(sender.getPhoneNumber())
            .receiverPhoneNumber(receiver.getPhoneNumber())
            .senderDebitCardId(sender.getLinkedDebitCardId())
            .receiverDebitCardId(receiver.getLinkedDebitCardId())
            .currency(Optional.ofNullable(incoming.getCurrency()).orElse(DEFAULT_CURRENCY))
            .amount(incoming.getAmount())
            .description(Optional.ofNullable(incoming.getDescription()).orElse("Yanki mobile payment"))
            .status(status)
            .createdAt(now)
            .updatedAt(now)
            .build();
  }
}