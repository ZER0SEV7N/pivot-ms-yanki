package com.pe.pivot.yanki.infrastructure.adapter.out.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.model.YankiPaymentStatus;
import com.pe.pivot.yanki.infrastructure.adapter.in.messaging.dto.YankiPaymentResultEvent;
import com.pe.pivot.yanki.infrastructure.adapter.out.messaging.dto.YankiPaymentRequestedEvent;
import io.reactivex.rxjava3.observers.TestObserver;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit test suite for YankiKafkaProducerAdapter.
 */
class YankiKafkaProducerAdapterTest {

  private static final String REQUESTED_TOPIC = "yanki.payment.requested";
  private static final String RESULT_TOPIC = "yanki.payment.result";

  private KafkaTemplate<String, Object> kafkaTemplate;
  private YankiKafkaProducerAdapter adapter;
  private YankiPayment payment;

  @BeforeEach
  void setUp() {
    kafkaTemplate = mock(KafkaTemplate.class);
    adapter = new YankiKafkaProducerAdapter(kafkaTemplate);
    ReflectionTestUtils.setField(adapter, "paymentRequestedTopic", REQUESTED_TOPIC);
    ReflectionTestUtils.setField(adapter, "paymentResultTopic", RESULT_TOPIC);
    payment = YankiPayment.builder()
            .transactionId("TX-1")
            .senderPhoneNumber("999111222")
            .receiverPhoneNumber("999333444")
            .senderDebitCardId("card-a")
            .receiverDebitCardId("card-b")
            .currency("PEN")
            .amount(new BigDecimal("30.00"))
            .description("Dinner")
            .status(YankiPaymentStatus.PENDING)
            .build();
  }

  @Test
  @DisplayName("Should be publish a payment requested event (happy path)")
  void shouldBePublishPaymentRequestedEvent() {
    // Arrange
    when(kafkaTemplate.send(anyString(), anyString(), any()))
            .thenReturn(CompletableFuture.completedFuture(null));

    // Act
    TestObserver<Void> observer = adapter.publishPaymentRequested(payment).test();

    // Assert
    observer.assertComplete();
    observer.assertNoErrors();
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(kafkaTemplate).send(eq(REQUESTED_TOPIC), eq("TX-1"), captor.capture());
    YankiPaymentRequestedEvent event = (YankiPaymentRequestedEvent) captor.getValue();
    assertEquals("TX-1", event.transactionId());
    assertEquals("999111222", event.senderPhoneNumber());
    assertEquals("999333444", event.receiverPhoneNumber());
    assertEquals("card-a", event.senderDebitCardId());
    assertEquals("card-b", event.receiverDebitCardId());
    assertEquals("PEN", event.currency());
    assertEquals(new BigDecimal("30.00"), event.amount());
    assertEquals("Dinner", event.description());
    assertEquals(true, event.requestedAt() != null);
  }

  @Test
  @DisplayName("Should be publish a payment result event with the current status")
  void shouldBePublishPaymentResultEvent() {
    // Arrange
    payment.setStatus(YankiPaymentStatus.COMPLETED);
    payment.setFailureReason("none");
    when(kafkaTemplate.send(anyString(), anyString(), any()))
            .thenReturn(CompletableFuture.completedFuture(null));

    // Act
    TestObserver<Void> observer = adapter.publishPaymentResult(payment).test();

    // Assert
    observer.assertComplete();
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(kafkaTemplate).send(eq(RESULT_TOPIC), eq("TX-1"), captor.capture());
    YankiPaymentResultEvent event = (YankiPaymentResultEvent) captor.getValue();
    assertEquals("COMPLETED", event.status());
    assertEquals("none", event.failureReason());
    assertEquals("TX-1", event.transactionId());
  }

  @Test
  @DisplayName("Should be propagate an error when the requested event cannot be published")
  void shouldBePropagateRequestedPublishError() {
    // Arrange
    when(kafkaTemplate.send(anyString(), anyString(), any()))
            .thenReturn(CompletableFuture.failedFuture(
                    new RuntimeException("kafka down")));

    // Act
    TestObserver<Void> observer = adapter.publishPaymentRequested(payment).test();

    // Assert
    observer.assertError(RuntimeException.class);
    observer.assertError(error -> "kafka down".equals(error.getMessage()));
  }

  @Test
  @DisplayName("Should be propagate an error when the result event cannot be published")
  void shouldBePropagateResultPublishError() {
    // Arrange
    when(kafkaTemplate.send(anyString(), anyString(), any()))
            .thenReturn(CompletableFuture.failedFuture(
                    new RuntimeException("kafka down")));

    // Act
    TestObserver<Void> observer = adapter.publishPaymentResult(payment).test();

    // Assert
    observer.assertError(RuntimeException.class);
  }
}
