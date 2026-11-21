package com.pe.pivot.yanki.infrastructure.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.yanki.domain.exception.BusinessRuleException;
import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.model.YankiPaymentStatus;
import com.pe.pivot.yanki.domain.port.in.RetrieveYankiPaymentUseCase;
import com.pe.pivot.yanki.domain.port.in.SendYankiPaymentUseCase;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiPaymentRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiPaymentResponse;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.mapper.YankiWebMapper;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for YankiPaymentController.
 */
class YankiPaymentControllerTest {

  private SendYankiPaymentUseCase sendPaymentUseCase;
  private RetrieveYankiPaymentUseCase retrievePaymentUseCase;
  private YankiPaymentController controller;
  private YankiPayment payment;

  @BeforeEach
  void setUp() {
    sendPaymentUseCase = mock(SendYankiPaymentUseCase.class);
    retrievePaymentUseCase = mock(RetrieveYankiPaymentUseCase.class);
    controller = new YankiPaymentController(
            sendPaymentUseCase, retrievePaymentUseCase, new YankiWebMapper());
    payment = YankiPayment.builder()
            .id("payment-1")
            .transactionId("TX-1")
            .senderPhoneNumber("999111222")
            .receiverPhoneNumber("999333444")
            .currency("PEN")
            .amount(new BigDecimal("30.00"))
            .status(YankiPaymentStatus.PENDING)
            .build();
  }

  @Test
  @DisplayName("Should be send a payment and return its response (happy path)")
  void shouldBeSendPayment() {
    // Arrange
    YankiPaymentRequest request = YankiPaymentRequest.builder()
            .senderPhoneNumber("999111222")
            .receiverPhoneNumber("999333444")
            .currency("PEN")
            .amount(new BigDecimal("30.00"))
            .description("Dinner")
            .build();
    when(sendPaymentUseCase.sendPayment(any(YankiPayment.class)))
            .thenReturn(Single.just(payment));

    // Act
    TestObserver<YankiPaymentResponse> observer =
            controller.sendPayment(request).test();

    // Assert
    observer.assertComplete();
    observer.assertValue(response -> {
      assertEquals("TX-1", response.transactionId());
      assertEquals(YankiPaymentStatus.PENDING, response.status());
      return true;
    });
    verify(sendPaymentUseCase).sendPayment(any(YankiPayment.class));
  }

  @Test
  @DisplayName("Should be return a payment by transaction id")
  void shouldBeGetPaymentByTransactionId() {
    // Arrange
    when(retrievePaymentUseCase.findByTransactionId("TX-1"))
            .thenReturn(Single.just(payment));

    // Act
    TestObserver<YankiPaymentResponse> observer =
            controller.getPaymentByTxId("TX-1").test();

    // Assert
    observer.assertComplete();
    observer.assertValue(response -> "payment-1".equals(response.id()));
    verify(retrievePaymentUseCase).findByTransactionId("TX-1");
  }

  @Test
  @DisplayName("Should be return every payment associated with a phone number")
  void shouldBeGetPaymentsByPhoneNumber() {
    // Arrange
    YankiPayment second = YankiPayment.builder()
            .id("payment-2")
            .transactionId("TX-2")
            .build();
    when(retrievePaymentUseCase.findByPhoneNumber("999111222"))
            .thenReturn(Flowable.just(payment, second));

    // Act
    TestSubscriber<YankiPaymentResponse> subscriber =
            controller.getPaymentsByPhone("999111222").test();

    // Assert
    subscriber.assertComplete();
    subscriber.assertValueCount(2);
    subscriber.assertValueAt(0, response -> "TX-1".equals(response.transactionId()));
    subscriber.assertValueAt(1, response -> "TX-2".equals(response.transactionId()));
  }

  @Test
  @DisplayName("Should be propagate BusinessRuleException when the payment is rejected")
  void shouldBePropagateBusinessRuleError() {
    // Arrange
    YankiPaymentRequest request = YankiPaymentRequest.builder()
            .senderPhoneNumber("999111222")
            .receiverPhoneNumber("999111222")
            .amount(new BigDecimal("30.00"))
            .build();
    when(sendPaymentUseCase.sendPayment(any(YankiPayment.class)))
            .thenReturn(Single.error(new BusinessRuleException("Invalid payment")));

    // Act
    TestObserver<YankiPaymentResponse> observer =
            controller.sendPayment(request).test();

    // Assert
    observer.assertError(BusinessRuleException.class);
    observer.assertNoValues();
  }
}
