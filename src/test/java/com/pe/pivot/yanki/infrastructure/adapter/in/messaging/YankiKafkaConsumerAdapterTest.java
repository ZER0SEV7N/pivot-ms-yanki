package com.pe.pivot.yanki.infrastructure.adapter.in.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.yanki.domain.model.YankiPaymentResultCommand;
import com.pe.pivot.yanki.domain.port.in.HandleYankiPaymentResultUseCase;
import com.pe.pivot.yanki.infrastructure.adapter.in.messaging.dto.YankiPaymentResultEvent;
import io.reactivex.rxjava3.core.Completable;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit test suite for YankiKafkaConsumerAdapter.
 */
class YankiKafkaConsumerAdapterTest {

  private HandleYankiPaymentResultUseCase handleResultUseCase;
  private YankiKafkaConsumerAdapter adapter;
  private YankiPaymentResultEvent event;

  @BeforeEach
  void setUp() {
    handleResultUseCase = mock(HandleYankiPaymentResultUseCase.class);
    adapter = new YankiKafkaConsumerAdapter(handleResultUseCase);
    event = YankiPaymentResultEvent.builder()
            .transactionId("TX-1")
            .status("COMPLETED")
            .senderPhoneNumber("999111222")
            .receiverPhoneNumber("999333444")
            .currency("PEN")
            .amount(new BigDecimal("30.00"))
            .failureReason(null)
            .processedAt(Instant.parse("2026-01-01T00:00:00Z"))
            .build();
  }

  @Test
  @DisplayName("Should be consume an event and delegate the result command (happy path)")
  void shouldBeConsumeEventAndDelegateCommand() {
    // Arrange
    when(handleResultUseCase.handleResult(any())).thenReturn(Completable.complete());

    // Act
    adapter.onPaymentResult(event);

    // Assert
    ArgumentCaptor<YankiPaymentResultCommand> captor =
            ArgumentCaptor.forClass(YankiPaymentResultCommand.class);
    verify(handleResultUseCase).handleResult(captor.capture());
    YankiPaymentResultCommand command = captor.getValue();
    assertEquals("TX-1", command.transactionId());
    assertEquals("COMPLETED", command.status());
    assertEquals(event.processedAt(), command.processedAt());
    assertEquals(true, command.isCompleted());
  }

  @Test
  @DisplayName("Should be swallow an error raised while handling the result event")
  void shouldBeSwallowHandlingError() {
    // Arrange
    when(handleResultUseCase.handleResult(any()))
            .thenReturn(Completable.error(new RuntimeException("processing failed")));

    // Act
    adapter.onPaymentResult(event);

    // Assert
    verify(handleResultUseCase).handleResult(any());
  }

  @Test
  @DisplayName("Should be forward a failed event with its failure reason")
  void shouldBeForwardFailedEvent() {
    // Arrange
    YankiPaymentResultEvent failedEvent = YankiPaymentResultEvent.builder()
            .transactionId("TX-2")
            .status("FAILED")
            .failureReason("Insufficient funds")
            .processedAt(Instant.parse("2026-01-02T00:00:00Z"))
            .build();
    when(handleResultUseCase.handleResult(any())).thenReturn(Completable.complete());

    // Act
    adapter.onPaymentResult(failedEvent);

    // Assert
    ArgumentCaptor<YankiPaymentResultCommand> captor =
            ArgumentCaptor.forClass(YankiPaymentResultCommand.class);
    verify(handleResultUseCase).handleResult(captor.capture());
    assertEquals("FAILED", captor.getValue().status());
    assertEquals("Insufficient funds", captor.getValue().failureReason());
  }
}
