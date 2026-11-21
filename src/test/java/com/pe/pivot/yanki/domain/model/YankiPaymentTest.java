package com.pe.pivot.yanki.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for YankiPayment domain model.
 */
class YankiPaymentTest {

  private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  @DisplayName("Should be build a payment with all fields (happy path)")
  void shouldBeBuildPaymentWithAllFields() {
    // Arrange & Act
    YankiPayment payment = YankiPayment.builder()
            .id("payment-1")
            .transactionId("TX-1")
            .senderPhoneNumber("999111222")
            .receiverPhoneNumber("999333444")
            .senderDebitCardId("card-sender")
            .receiverDebitCardId("card-receiver")
            .currency("PEN")
            .amount(new BigDecimal("25.00"))
            .description("Dinner")
            .status(YankiPaymentStatus.PENDING)
            .failureReason("none")
            .createdAt(CREATED_AT)
            .updatedAt(CREATED_AT)
            .build();

    // Assert
    assertEquals("payment-1", payment.getId());
    assertEquals("TX-1", payment.getTransactionId());
    assertEquals("999111222", payment.getSenderPhoneNumber());
    assertEquals("999333444", payment.getReceiverPhoneNumber());
    assertEquals("card-sender", payment.getSenderDebitCardId());
    assertEquals("card-receiver", payment.getReceiverDebitCardId());
    assertEquals("PEN", payment.getCurrency());
    assertEquals(new BigDecimal("25.00"), payment.getAmount());
    assertEquals("Dinner", payment.getDescription());
    assertEquals(YankiPaymentStatus.PENDING, payment.getStatus());
    assertEquals("none", payment.getFailureReason());
    assertEquals(CREATED_AT, payment.getCreatedAt());
    assertEquals(CREATED_AT, payment.getUpdatedAt());
  }

  @Test
  @DisplayName("Should be set and read every field through the no-args constructor")
  void shouldBeSetAndGetEveryField() {
    // Arrange
    YankiPayment payment = new YankiPayment();

    // Act
    payment.setId("payment-2");
    payment.setTransactionId("TX-2");
    payment.setSenderPhoneNumber("999555666");
    payment.setReceiverPhoneNumber("999777888");
    payment.setSenderDebitCardId("card-a");
    payment.setReceiverDebitCardId("card-b");
    payment.setCurrency("USD");
    payment.setAmount(BigDecimal.ONE);
    payment.setDescription("Lunch");
    payment.setStatus(YankiPaymentStatus.COMPLETED);
    payment.setFailureReason(null);
    payment.setCreatedAt(CREATED_AT);
    payment.setUpdatedAt(CREATED_AT);

    // Assert
    assertEquals("payment-2", payment.getId());
    assertEquals("TX-2", payment.getTransactionId());
    assertEquals("999555666", payment.getSenderPhoneNumber());
    assertEquals("999777888", payment.getReceiverPhoneNumber());
    assertEquals("card-a", payment.getSenderDebitCardId());
    assertEquals("card-b", payment.getReceiverDebitCardId());
    assertEquals("USD", payment.getCurrency());
    assertEquals(BigDecimal.ONE, payment.getAmount());
    assertEquals("Lunch", payment.getDescription());
    assertEquals(YankiPaymentStatus.COMPLETED, payment.getStatus());
    assertEquals(CREATED_AT, payment.getCreatedAt());
  }

  @Test
  @DisplayName("Should be create a payment through the all-args constructor")
  void shouldBeCreatePaymentWithAllArgsConstructor() {
    // Arrange & Act
    YankiPayment payment = new YankiPayment("payment-3", "TX-3", "111", "222",
            null, null, "PEN", BigDecimal.TEN, "Transfer", YankiPaymentStatus.FAILED,
            "Rejected", CREATED_AT, CREATED_AT);

    // Assert
    assertEquals("payment-3", payment.getId());
    assertEquals(YankiPaymentStatus.FAILED, payment.getStatus());
    assertEquals("Rejected", payment.getFailureReason());
  }

  @Test
  @DisplayName("Should be report bank settlement when either debit card is present")
  void shouldBeReportBankSettlementRequirement() {
    // Arrange
    YankiPayment withSenderCard = YankiPayment.builder().senderDebitCardId("card-1").build();
    YankiPayment withReceiverCard = YankiPayment.builder()
            .receiverDebitCardId("card-2").build();
    YankiPayment withBlankCards = YankiPayment.builder()
            .senderDebitCardId("  ")
            .receiverDebitCardId("  ")
            .build();
    final YankiPayment withoutCards = new YankiPayment();

    // Act & Assert
    assertTrue(withSenderCard.requiresBankSettlement());
    assertTrue(withReceiverCard.requiresBankSettlement());
    assertFalse(withBlankCards.requiresBankSettlement());
    assertFalse(withoutCards.requiresBankSettlement());
  }
}
