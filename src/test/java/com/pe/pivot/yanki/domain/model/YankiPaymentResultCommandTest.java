package com.pe.pivot.yanki.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for YankiPaymentResultCommand domain record.
 */
class YankiPaymentResultCommandTest {

  private static final Instant PROCESSED_AT = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  @DisplayName("Should be build a command and report it as completed (happy path)")
  void shouldBeBuildCommandAndReportCompleted() {
    // Arrange & Act
    YankiPaymentResultCommand command = YankiPaymentResultCommand.builder()
            .transactionId("TX-1")
            .status("COMPLETED")
            .failureReason(null)
            .processedAt(PROCESSED_AT)
            .build();

    // Assert
    assertEquals("TX-1", command.transactionId());
    assertEquals("COMPLETED", command.status());
    assertEquals(PROCESSED_AT, command.processedAt());
    assertTrue(command.isCompleted());
  }

  @Test
  @DisplayName("Should be treat the completed status as case insensitive")
  void shouldBeTreatCompletedStatusAsCaseInsensitive() {
    // Arrange
    YankiPaymentResultCommand command =
            new YankiPaymentResultCommand("TX-1", "completed", null, PROCESSED_AT);

    // Act & Assert
    assertTrue(command.isCompleted());
  }

  @Test
  @DisplayName("Should be report a command that is not completed as not completed")
  void shouldBeReportNotCompleted() {
    // Arrange
    YankiPaymentResultCommand failed =
            new YankiPaymentResultCommand("TX-1", "FAILED", "Rejected", PROCESSED_AT);
    YankiPaymentResultCommand withoutStatus =
            new YankiPaymentResultCommand("TX-2", null, null, PROCESSED_AT);

    // Act & Assert
    assertFalse(failed.isCompleted());
    assertEquals("Rejected", failed.failureReason());
    assertFalse(withoutStatus.isCompleted());
  }

  @Test
  @DisplayName("Should be compare command records by value")
  void shouldBeCompareCommandRecordsByValue() {
    // Arrange
    YankiPaymentResultCommand first =
            new YankiPaymentResultCommand("TX-1", "COMPLETED", null, PROCESSED_AT);
    YankiPaymentResultCommand second =
            new YankiPaymentResultCommand("TX-1", "COMPLETED", null, PROCESSED_AT);
    YankiPaymentResultCommand different =
            new YankiPaymentResultCommand("TX-2", "COMPLETED", null, PROCESSED_AT);

    // Act & Assert
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
    assertNotEquals(first, different);
    assertNotNull(first.toString());
  }
}
