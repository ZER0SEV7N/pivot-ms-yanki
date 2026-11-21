package com.pe.pivot.yanki.application.usecase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.yanki.domain.exception.PaymentNotFoundException;
import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.port.out.YankiPaymentRepositoryPort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for RetrieveYankiPaymentUseCaseImpl.
 */
class RetrieveYankiPaymentUseCaseImplTest {

  private YankiPaymentRepositoryPort paymentRepository;
  private RetrieveYankiPaymentUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    paymentRepository = mock(YankiPaymentRepositoryPort.class);
    useCase = new RetrieveYankiPaymentUseCaseImpl(paymentRepository);
  }

  @Test
  @DisplayName("Should be find a payment by its transaction id (happy path)")
  void shouldBeFindPaymentByTransactionId() {
    // Arrange
    YankiPayment payment = YankiPayment.builder().transactionId("TX-1").build();
    when(paymentRepository.findByTransactionId("TX-1")).thenReturn(Maybe.just(payment));

    // Act
    TestObserver<YankiPayment> observer = useCase.findByTransactionId("TX-1").test();

    // Assert
    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertValue(payment);
    verify(paymentRepository).findByTransactionId("TX-1");
  }

  @Test
  @DisplayName("Should be fail with PaymentNotFoundException when the payment is missing")
  void shouldBeFailWhenPaymentIsMissing() {
    // Arrange
    when(paymentRepository.findByTransactionId("TX-404")).thenReturn(Maybe.empty());

    // Act
    TestObserver<YankiPayment> observer = useCase.findByTransactionId("TX-404").test();

    // Assert
    observer.assertError(PaymentNotFoundException.class);
    observer.assertError(error -> error.getMessage().contains("TX-404"));
    observer.assertNoValues();
  }

  @Test
  @DisplayName("Should be emit every payment associated with a phone number")
  void shouldBeEmitPaymentsByPhoneNumber() {
    // Arrange
    YankiPayment first = YankiPayment.builder().transactionId("TX-1").build();
    YankiPayment second = YankiPayment.builder().transactionId("TX-2").build();
    when(paymentRepository.findByPhoneNumber("999111222"))
            .thenReturn(Flowable.just(first, second));

    // Act
    TestSubscriber<YankiPayment> subscriber =
            useCase.findByPhoneNumber("999111222").test();

    // Assert
    subscriber.assertComplete();
    subscriber.assertNoErrors();
    subscriber.assertValues(first, second);
  }
}
