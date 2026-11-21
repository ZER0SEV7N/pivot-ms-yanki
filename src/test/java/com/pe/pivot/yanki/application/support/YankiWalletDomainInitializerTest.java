package com.pe.pivot.yanki.application.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pe.pivot.yanki.domain.exception.BusinessRuleException;
import com.pe.pivot.yanki.domain.model.YankiDocumentType;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.model.YankiWalletStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for YankiWalletDomainInitializer.
 */
class YankiWalletDomainInitializerTest {

  private YankiWalletDomainInitializer initializer;

  @BeforeEach
  void setUp() {
    initializer = new YankiWalletDomainInitializer();
  }

  @Test
  @DisplayName("Should be initialize a wallet with trimmed fields and defaults (happy path)")
  void shouldBeInitializeWalletWithDefaults() {
    // Arrange
    YankiWallet wallet = YankiWallet.builder()
            .documentType(YankiDocumentType.DNI)
            .documentNumber("12345678")
            .phoneNumber("  999111222  ")
            .email("  JOHN@MAIL.COM  ")
            .imei("  IMEI-1  ")
            .build();

    // Act
    initializer.initialize(wallet);

    // Assert
    assertEquals("999111222", wallet.getPhoneNumber());
    assertEquals("john@mail.com", wallet.getEmail());
    assertEquals("IMEI-1", wallet.getImei());
    assertEquals(BigDecimal.ZERO, wallet.getBalance());
    assertEquals(YankiWalletStatus.ACTIVE, wallet.getStatus());
    assertNotNull(wallet.getCreatedAt());
    assertNotNull(wallet.getUpdatedAt());
  }

  @Test
  @DisplayName("Should be keep an explicitly provided initial balance")
  void shouldBeKeepProvidedBalance() {
    // Arrange
    YankiWallet wallet = YankiWallet.builder()
            .documentType(YankiDocumentType.PASSPORT)
            .documentNumber("AB123456")
            .phoneNumber("999111222")
            .email("john@mail.com")
            .imei("IMEI-1")
            .balance(new BigDecimal("150.00"))
            .build();

    // Act
    initializer.initialize(wallet);

    // Assert
    assertEquals(new BigDecimal("150.00"), wallet.getBalance());
    assertTrue(wallet.isActive());
  }

  @Test
  @DisplayName("Should be reject a DNI that does not contain exactly eight digits")
  void shouldBeRejectInvalidDni() {
    // Arrange
    YankiWallet wallet = YankiWallet.builder()
            .documentType(YankiDocumentType.DNI)
            .documentNumber("1234")
            .phoneNumber("999111222")
            .email("john@mail.com")
            .imei("IMEI-1")
            .build();

    // Act & Assert
    BusinessRuleException error = assertThrows(
            BusinessRuleException.class, () -> initializer.initialize(wallet));
    assertEquals("DNI must contain exactly 8 numeric digits.", error.getMessage());
  }

  @Test
  @DisplayName("Should be reject a CEX document with an invalid format")
  void shouldBeRejectInvalidCexDocument() {
    // Arrange
    YankiWallet wallet = YankiWallet.builder()
            .documentType(YankiDocumentType.CEX)
            .documentNumber("A1")
            .phoneNumber("999111222")
            .email("john@mail.com")
            .imei("IMEI-1")
            .build();

    // Act & Assert
    BusinessRuleException error = assertThrows(
            BusinessRuleException.class, () -> initializer.initialize(wallet));
    assertTrue(error.getMessage().contains("6 to 12 alphanumeric characters"));
  }

  @Test
  @DisplayName("Should be reject a PASSPORT document with an invalid format")
  void shouldBeRejectInvalidPassportDocument() {
    // Arrange
    YankiWallet wallet = YankiWallet.builder()
            .documentType(YankiDocumentType.PASSPORT)
            .documentNumber("ABC")
            .phoneNumber("999111222")
            .email("john@mail.com")
            .imei("IMEI-1")
            .build();

    // Act & Assert
    assertThrows(BusinessRuleException.class, () -> initializer.initialize(wallet));
  }
}
