package com.pe.pivot.yanki.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pe.pivot.yanki.domain.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for YankiWallet domain model.
 */
class YankiWalletTest {

  private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  @DisplayName("Should be build a wallet with all fields (happy path)")
  void shouldBeBuildWalletWithAllFields() {
    // Arrange & Act
    YankiWallet wallet = YankiWallet.builder()
            .id("wallet-1")
            .documentType(YankiDocumentType.DNI)
            .documentNumber("12345678")
            .phoneNumber("999111222")
            .imei("IMEI-1")
            .email("john@mail.com")
            .balance(new BigDecimal("100.00"))
            .linkedDebitCardId("card-1")
            .status(YankiWalletStatus.ACTIVE)
            .createdAt(CREATED_AT)
            .updatedAt(CREATED_AT)
            .build();

    // Assert
    assertEquals("wallet-1", wallet.getId());
    assertEquals(YankiDocumentType.DNI, wallet.getDocumentType());
    assertEquals("12345678", wallet.getDocumentNumber());
    assertEquals("999111222", wallet.getPhoneNumber());
    assertEquals("IMEI-1", wallet.getImei());
    assertEquals("john@mail.com", wallet.getEmail());
    assertEquals(new BigDecimal("100.00"), wallet.getBalance());
    assertEquals("card-1", wallet.getLinkedDebitCardId());
    assertEquals(YankiWalletStatus.ACTIVE, wallet.getStatus());
    assertEquals(CREATED_AT, wallet.getCreatedAt());
    assertEquals(CREATED_AT, wallet.getUpdatedAt());
  }

  @Test
  @DisplayName("Should be set and read every field through the no-args constructor")
  void shouldBeSetAndGetEveryField() {
    // Arrange
    YankiWallet wallet = new YankiWallet();

    // Act
    wallet.setId("wallet-2");
    wallet.setDocumentType(YankiDocumentType.CEX);
    wallet.setDocumentNumber("AB1234");
    wallet.setPhoneNumber("999333444");
    wallet.setImei("IMEI-2");
    wallet.setEmail("jane@mail.com");
    wallet.setBalance(new BigDecimal("50.00"));
    wallet.setLinkedDebitCardId("card-2");
    wallet.setStatus(YankiWalletStatus.BLOCKED);
    wallet.setCreatedAt(CREATED_AT);
    wallet.setUpdatedAt(CREATED_AT);

    // Assert
    assertEquals("wallet-2", wallet.getId());
    assertEquals(YankiDocumentType.CEX, wallet.getDocumentType());
    assertEquals("AB1234", wallet.getDocumentNumber());
    assertEquals("999333444", wallet.getPhoneNumber());
    assertEquals("IMEI-2", wallet.getImei());
    assertEquals("jane@mail.com", wallet.getEmail());
    assertEquals(new BigDecimal("50.00"), wallet.getBalance());
    assertEquals("card-2", wallet.getLinkedDebitCardId());
    assertEquals(YankiWalletStatus.BLOCKED, wallet.getStatus());
    assertEquals(CREATED_AT, wallet.getCreatedAt());
    assertEquals(CREATED_AT, wallet.getUpdatedAt());
  }

  @Test
  @DisplayName("Should be create a wallet through the all-args constructor")
  void shouldBeCreateWalletWithAllArgsConstructor() {
    // Arrange & Act
    YankiWallet wallet = new YankiWallet("wallet-3", YankiDocumentType.PASSPORT,
            "AB123456", "999555666", "IMEI-3", "bob@mail.com",
            BigDecimal.TEN, "card-3", YankiWalletStatus.ACTIVE, CREATED_AT, CREATED_AT);

    // Assert
    assertEquals("wallet-3", wallet.getId());
    assertEquals(YankiDocumentType.PASSPORT, wallet.getDocumentType());
    assertEquals(BigDecimal.TEN, wallet.getBalance());
    assertTrue(wallet.isActive());
  }

  @Test
  @DisplayName("Should be debit the requested amount from the balance")
  void shouldBeDebitBalance() {
    // Arrange
    YankiWallet wallet = YankiWallet.builder()
            .balance(new BigDecimal("100.00"))
            .build();

    // Act
    wallet.debit(new BigDecimal("30.00"));

    // Assert
    assertEquals(new BigDecimal("70.00"), wallet.getBalance());
    assertNotNull(wallet.getUpdatedAt());
  }

  @Test
  @DisplayName("Should be throw BusinessRuleException when the balance is insufficient")
  void shouldBeThrowWhenBalanceIsInsufficient() {
    // Arrange
    YankiWallet wallet = YankiWallet.builder()
            .balance(new BigDecimal("10.00"))
            .build();

    // Act & Assert
    BusinessRuleException error = assertThrows(
            BusinessRuleException.class, () -> wallet.debit(new BigDecimal("20.00")));
    assertTrue(error.getMessage().contains("Insufficient Yanki wallet balance"));
    assertEquals(new BigDecimal("10.00"), wallet.getBalance());
  }

  @Test
  @DisplayName("Should be treat a null balance as zero when debiting")
  void shouldBeTreatNullBalanceAsZeroWhenDebiting() {
    // Arrange
    YankiWallet wallet = new YankiWallet();

    // Act
    wallet.debit(BigDecimal.ZERO);

    // Assert
    assertEquals(BigDecimal.ZERO, wallet.getBalance());
  }

  @Test
  @DisplayName("Should be credit the requested amount to a null balance")
  void shouldBeCreditAmountToNullBalance() {
    // Arrange
    YankiWallet wallet = new YankiWallet();

    // Act
    wallet.credit(new BigDecimal("15.00"));

    // Assert
    assertEquals(new BigDecimal("15.00"), wallet.getBalance());
    assertNotNull(wallet.getUpdatedAt());
  }

  @Test
  @DisplayName("Should be report whether the wallet is active")
  void shouldBeReportActiveStatus() {
    // Arrange
    YankiWallet active = YankiWallet.builder().status(YankiWalletStatus.ACTIVE).build();
    YankiWallet blocked = YankiWallet.builder().status(YankiWalletStatus.BLOCKED).build();
    YankiWallet withoutStatus = new YankiWallet();

    // Act & Assert
    assertTrue(active.isActive());
    assertFalse(blocked.isActive());
    assertFalse(withoutStatus.isActive());
  }

  @Test
  @DisplayName("Should be report whether a linked debit card exists")
  void shouldBeReportLinkedCardStatus() {
    // Arrange
    YankiWallet withCard = YankiWallet.builder().linkedDebitCardId("card-1").build();
    YankiWallet withBlankCard = YankiWallet.builder().linkedDebitCardId("  ").build();
    YankiWallet withoutCard = new YankiWallet();

    // Act & Assert
    assertTrue(withCard.hasLinkedCard());
    assertFalse(withBlankCard.hasLinkedCard());
    assertFalse(withoutCard.hasLinkedCard());
    assertNull(withoutCard.getLinkedDebitCardId());
  }
}
