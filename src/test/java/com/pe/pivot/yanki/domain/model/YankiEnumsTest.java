package com.pe.pivot.yanki.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for the Yanki domain enums.
 */
class YankiEnumsTest {

  @Test
  @DisplayName("Should be expose every supported document type (happy path)")
  void shouldBeExposeDocumentTypes() {
    // Act
    YankiDocumentType[] values = YankiDocumentType.values();

    // Assert
    assertEquals(3, values.length);
    assertEquals(YankiDocumentType.DNI, values[0]);
    assertEquals(YankiDocumentType.CEX, values[1]);
    assertEquals(YankiDocumentType.PASSPORT, values[2]);
    assertEquals("DNI", YankiDocumentType.valueOf("DNI").name());
  }

  @Test
  @DisplayName("Should be expose every supported wallet status")
  void shouldBeExposeWalletStatuses() {
    // Act
    YankiWalletStatus[] values = YankiWalletStatus.values();

    // Assert
    assertEquals(3, values.length);
    assertEquals(YankiWalletStatus.ACTIVE, values[0]);
    assertEquals(YankiWalletStatus.BLOCKED, values[1]);
    assertEquals(YankiWalletStatus.CLOSED, values[2]);
    assertEquals("ACTIVE", YankiWalletStatus.valueOf("ACTIVE").name());
  }

  @Test
  @DisplayName("Should be expose every supported payment status")
  void shouldBeExposePaymentStatuses() {
    // Act
    YankiPaymentStatus[] values = YankiPaymentStatus.values();

    // Assert
    assertEquals(3, values.length);
    assertEquals(YankiPaymentStatus.PENDING, values[0]);
    assertEquals(YankiPaymentStatus.COMPLETED, values[1]);
    assertEquals(YankiPaymentStatus.FAILED, values[2]);
    assertEquals("PENDING", YankiPaymentStatus.valueOf("PENDING").name());
  }

  @Test
  @DisplayName("Should be throw IllegalArgumentException for an unknown enum value")
  void shouldBeThrowForUnknownEnumValue() {
    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> YankiDocumentType.valueOf("UNKNOWN"));
    assertThrows(IllegalArgumentException.class, () -> YankiWalletStatus.valueOf("UNKNOWN"));
    assertThrows(IllegalArgumentException.class, () -> YankiPaymentStatus.valueOf("UNKNOWN"));
  }
}
