package com.pe.pivot.yanki.application.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.model.YankiPaymentStatus;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for YankiPaymentFactory.
 */
class YankiPaymentFactoryTest {

  private YankiPaymentFactory factory;
  private YankiWallet sender;
  private YankiWallet receiver;

  @BeforeEach
  void setUp() {
    factory = new YankiPaymentFactory();
    sender = YankiWallet.builder()
            .phoneNumber("999111222")
            .linkedDebitCardId("card-sender")
            .build();
    receiver = YankiWallet.builder()
            .phoneNumber("999333444")
            .linkedDebitCardId("card-receiver")
            .build();
  }

  @Test
  @DisplayName("Should be create a payment with all provided fields (happy path)")
  void shouldBeCreatePaymentWithAllProvidedFields() {
    // Arrange
    YankiPayment incoming = YankiPayment.builder()
            .currency("USD")
            .amount(new BigDecimal("25.50"))
            .description("Dinner")
            .build();

    // Act
    YankiPayment payment = factory.create(
            incoming, sender, receiver, YankiPaymentStatus.PENDING);

    // Assert
    assertTrue(payment.getTransactionId().startsWith("YK-"));
    assertEquals("999111222", payment.getSenderPhoneNumber());
    assertEquals("999333444", payment.getReceiverPhoneNumber());
    assertEquals("card-sender", payment.getSenderDebitCardId());
    assertEquals("card-receiver", payment.getReceiverDebitCardId());
    assertEquals("USD", payment.getCurrency());
    assertEquals(new BigDecimal("25.50"), payment.getAmount());
    assertEquals("Dinner", payment.getDescription());
    assertEquals(YankiPaymentStatus.PENDING, payment.getStatus());
    assertNotNull(payment.getCreatedAt());
    assertNotNull(payment.getUpdatedAt());
  }

  @Test
  @DisplayName("Should be apply default currency and description when they are absent")
  void shouldBeApplyDefaultsWhenFieldsAreAbsent() {
    // Arrange
    YankiPayment incoming = YankiPayment.builder()
            .amount(new BigDecimal("10.00"))
            .build();

    // Act
    YankiPayment payment = factory.create(
            incoming, sender, receiver, YankiPaymentStatus.COMPLETED);

    // Assert
    assertEquals("PEN", payment.getCurrency());
    assertEquals("Yanki mobile payment", payment.getDescription());
    assertEquals(YankiPaymentStatus.COMPLETED, payment.getStatus());
  }

  @Test
  @DisplayName("Should be generate a unique transaction id for every payment")
  void shouldBeGenerateUniqueTransactionIds() {
    // Arrange
    YankiPayment incoming = YankiPayment.builder()
            .amount(new BigDecimal("1.00"))
            .build();

    // Act
    YankiPayment first = factory.create(
            incoming, sender, receiver, YankiPaymentStatus.PENDING);
    YankiPayment second = factory.create(
            incoming, sender, receiver, YankiPaymentStatus.PENDING);

    // Assert
    assertNotEquals(first.getTransactionId(), second.getTransactionId());
  }
}
