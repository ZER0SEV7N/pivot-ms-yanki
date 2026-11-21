package com.pe.pivot.yanki.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.model.YankiPaymentResultCommand;
import com.pe.pivot.yanki.domain.model.YankiPaymentStatus;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.port.out.YankiPaymentRepositoryPort;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for HandleYankiPaymentResultUseCaseImpl.
 */
class HandleYankiPaymentResultUseCaseImplTest {

  private YankiPaymentRepositoryPort paymentRepository;
  private YankiWalletRepositoryPort walletRepository;
  private HandleYankiPaymentResultUseCaseImpl useCase;
  private YankiPayment payment;
  private Instant processedAt;

  @BeforeEach
  void setUp() {
    paymentRepository = mock(YankiPaymentRepositoryPort.class);
    walletRepository = mock(YankiWalletRepositoryPort.class);
    useCase = new HandleYankiPaymentResultUseCaseImpl(paymentRepository, walletRepository);
    processedAt = Instant.parse("2026-01-01T00:00:00Z");
    payment = YankiPayment.builder()
            .transactionId("TX-1")
            .senderPhoneNumber("999111222")
            .receiverPhoneNumber("999333444")
            .amount(new BigDecimal("40.00"))
            .status(YankiPaymentStatus.PENDING)
            .build();
  }

  @Test
  @DisplayName("Should be complete the payment and credit the receiver wallet (happy path)")
  void shouldBeCompletePaymentAndCreditReceiverWallet() {
    // Arrange
    YankiWallet receiverWallet = YankiWallet.builder()
            .phoneNumber("999333444")
            .balance(new BigDecimal("10.00"))
            .build();
    YankiPaymentResultCommand command =
            new YankiPaymentResultCommand("TX-1", "COMPLETED", null, processedAt);
    when(paymentRepository.findByTransactionId("TX-1")).thenReturn(Maybe.just(payment));
    when(walletRepository.findByPhoneNumber("999333444")).thenReturn(Maybe.just(receiverWallet));
    when(walletRepository.save(receiverWallet)).thenReturn(Single.just(receiverWallet));
    when(paymentRepository.save(payment)).thenReturn(Single.just(payment));

    // Act
    TestObserver<Void> observer = useCase.handleResult(command).test();

    // Assert
    observer.assertComplete();
    observer.assertNoErrors();
    assertEquals(YankiPaymentStatus.COMPLETED, payment.getStatus());
    assertEquals(new BigDecimal("50.00"), receiverWallet.getBalance());
    verify(walletRepository).save(receiverWallet);
    verify(paymentRepository).save(payment);
  }

  @Test
  @DisplayName("Should be avoid crediting the receiver when a debit card is linked")
  void shouldBeAvoidCreditWhenReceiverHasLinkedCard() {
    // Arrange
    payment.setReceiverDebitCardId("card-receiver");
    YankiPaymentResultCommand command =
            new YankiPaymentResultCommand("TX-1", "COMPLETED", null, processedAt);
    when(paymentRepository.findByTransactionId("TX-1")).thenReturn(Maybe.just(payment));
    when(paymentRepository.save(payment)).thenReturn(Single.just(payment));

    // Act
    TestObserver<Void> observer = useCase.handleResult(command).test();

    // Assert
    observer.assertComplete();
    assertEquals(YankiPaymentStatus.COMPLETED, payment.getStatus());
    verify(walletRepository, never()).findByPhoneNumber(any());
    verify(walletRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be fail the payment and refund the sender wallet when it has no card")
  void shouldBeFailPaymentAndRefundSenderWallet() {
    // Arrange
    YankiWallet senderWallet = YankiWallet.builder()
            .phoneNumber("999111222")
            .balance(new BigDecimal("20.00"))
            .build();
    YankiPaymentResultCommand command =
            new YankiPaymentResultCommand("TX-1", "FAILED", "Insufficient funds", processedAt);
    when(paymentRepository.findByTransactionId("TX-1")).thenReturn(Maybe.just(payment));
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(senderWallet));
    when(walletRepository.save(senderWallet)).thenReturn(Single.just(senderWallet));
    when(paymentRepository.save(payment)).thenReturn(Single.just(payment));

    // Act
    TestObserver<Void> observer = useCase.handleResult(command).test();

    // Assert
    observer.assertComplete();
    assertEquals(YankiPaymentStatus.FAILED, payment.getStatus());
    assertEquals("Insufficient funds", payment.getFailureReason());
    assertEquals(new BigDecimal("60.00"), senderWallet.getBalance());
    verify(walletRepository).save(senderWallet);
  }

  @Test
  @DisplayName("Should be avoid refunding the sender when a debit card is linked")
  void shouldBeAvoidRefundWhenSenderHasLinkedCard() {
    // Arrange
    payment.setSenderDebitCardId("card-sender");
    YankiPaymentResultCommand command =
            new YankiPaymentResultCommand("TX-1", "FAILED", "Rejected", processedAt);
    when(paymentRepository.findByTransactionId("TX-1")).thenReturn(Maybe.just(payment));
    when(paymentRepository.save(payment)).thenReturn(Single.just(payment));

    // Act
    TestObserver<Void> observer = useCase.handleResult(command).test();

    // Assert
    observer.assertComplete();
    assertEquals(YankiPaymentStatus.FAILED, payment.getStatus());
    verify(walletRepository, never()).findByPhoneNumber(any());
  }

  @Test
  @DisplayName("Should be ignore a result when the payment is already finalized")
  void shouldBeIgnoreAlreadyFinalizedPayment() {
    // Arrange
    payment.setStatus(YankiPaymentStatus.COMPLETED);
    YankiPaymentResultCommand command =
            new YankiPaymentResultCommand("TX-1", "FAILED", "Late result", processedAt);
    when(paymentRepository.findByTransactionId("TX-1")).thenReturn(Maybe.just(payment));

    // Act
    TestObserver<Void> observer = useCase.handleResult(command).test();

    // Assert
    observer.assertComplete();
    verify(paymentRepository, never()).save(any());
    verify(walletRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be complete silently when the payment does not exist")
  void shouldBeCompleteSilentlyWhenPaymentDoesNotExist() {
    // Arrange
    YankiPaymentResultCommand command =
            new YankiPaymentResultCommand("TX-404", "COMPLETED", null, processedAt);
    when(paymentRepository.findByTransactionId("TX-404")).thenReturn(Maybe.empty());

    // Act
    TestObserver<Void> observer = useCase.handleResult(command).test();

    // Assert
    observer.assertComplete();
    observer.assertNoErrors();
    verify(paymentRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be propagate an error when saving the finalized payment fails")
  void shouldBePropagateSaveError() {
    // Arrange
    RuntimeException failure = new RuntimeException("write failed");
    payment.setReceiverDebitCardId("card-receiver");
    YankiPaymentResultCommand command =
            new YankiPaymentResultCommand("TX-1", "COMPLETED", null, processedAt);
    when(paymentRepository.findByTransactionId("TX-1")).thenReturn(Maybe.just(payment));
    when(paymentRepository.save(payment)).thenReturn(Single.error(failure));

    // Act
    TestObserver<Void> observer = useCase.handleResult(command).test();

    // Assert
    observer.assertError(failure);
  }
}
