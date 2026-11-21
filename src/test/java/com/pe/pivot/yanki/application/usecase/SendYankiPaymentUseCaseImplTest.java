package com.pe.pivot.yanki.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.yanki.application.support.YankiPaymentFactory;
import com.pe.pivot.yanki.domain.exception.BusinessRuleException;
import com.pe.pivot.yanki.domain.exception.WalletNotFoundException;
import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.model.YankiPaymentStatus;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.model.YankiWalletStatus;
import com.pe.pivot.yanki.domain.port.out.YankiEventPublisherPort;
import com.pe.pivot.yanki.domain.port.out.YankiPaymentRepositoryPort;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for SendYankiPaymentUseCaseImpl.
 */
class SendYankiPaymentUseCaseImplTest {

  private YankiWalletRepositoryPort walletRepository;
  private YankiPaymentRepositoryPort paymentRepository;
  private YankiEventPublisherPort eventPublisher;
  private YankiPaymentFactory paymentFactory;
  private SendYankiPaymentUseCaseImpl useCase;
  private YankiPayment incoming;

  @BeforeEach
  void setUp() {
    walletRepository = mock(YankiWalletRepositoryPort.class);
    paymentRepository = mock(YankiPaymentRepositoryPort.class);
    eventPublisher = mock(YankiEventPublisherPort.class);
    paymentFactory = mock(YankiPaymentFactory.class);
    useCase = new SendYankiPaymentUseCaseImpl(
            walletRepository, paymentRepository, eventPublisher, paymentFactory);
    incoming = YankiPayment.builder()
            .senderPhoneNumber("999111222")
            .receiverPhoneNumber("999333444")
            .amount(new BigDecimal("30.00"))
            .currency("PEN")
            .build();
  }

  @Test
  @DisplayName("Should be transfer funds instantly between wallets without linked cards "
          + "(happy path)")
  void shouldBeTransferInstantlyBetweenWallets() {
    // Arrange
    YankiWallet sender = wallet("999111222", "100.00");
    YankiWallet receiver = wallet("999333444", "0.00");
    YankiPayment payment = completedPayment();
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(sender));
    when(walletRepository.findByPhoneNumber("999333444")).thenReturn(Maybe.just(receiver));
    when(paymentFactory.create(incoming, sender, receiver, YankiPaymentStatus.COMPLETED))
            .thenReturn(payment);
    when(walletRepository.save(sender)).thenReturn(Single.just(sender));
    when(walletRepository.save(receiver)).thenReturn(Single.just(receiver));
    when(paymentRepository.save(payment)).thenReturn(Single.just(payment));
    when(eventPublisher.publishPaymentResult(payment)).thenReturn(Completable.complete());

    // Act
    TestObserver<YankiPayment> observer = useCase.sendPayment(incoming).test();

    // Assert
    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertValue(payment);
    assertEquals(new BigDecimal("70.00"), sender.getBalance());
    assertEquals(new BigDecimal("30.00"), receiver.getBalance());
    verify(eventPublisher).publishPaymentResult(payment);
  }

  @Test
  @DisplayName("Should be create a pending payment when the sender has a linked card")
  void shouldBeCreatePendingPaymentWhenSenderHasLinkedCard() {
    // Arrange
    YankiWallet sender = wallet("999111222", "100.00");
    sender.setLinkedDebitCardId("card-sender");
    YankiWallet receiver = wallet("999333444", "0.00");
    YankiPayment pending = pendingPayment();
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(sender));
    when(walletRepository.findByPhoneNumber("999333444")).thenReturn(Maybe.just(receiver));
    when(paymentFactory.create(incoming, sender, receiver, YankiPaymentStatus.PENDING))
            .thenReturn(pending);
    when(paymentRepository.save(pending)).thenReturn(Single.just(pending));
    when(eventPublisher.publishPaymentRequested(pending)).thenReturn(Completable.complete());

    // Act
    TestObserver<YankiPayment> observer = useCase.sendPayment(incoming).test();

    // Assert
    observer.assertComplete();
    observer.assertValue(pending);
    assertEquals(new BigDecimal("100.00"), sender.getBalance());
    verify(walletRepository, never()).save(any());
    verify(eventPublisher).publishPaymentRequested(pending);
  }

  @Test
  @DisplayName("Should be debit the sender and create a pending payment when only the receiver "
          + "has a linked card")
  void shouldBeDebitSenderWhenOnlyReceiverHasLinkedCard() {
    // Arrange
    YankiWallet sender = wallet("999111222", "100.00");
    YankiWallet receiver = wallet("999333444", "0.00");
    receiver.setLinkedDebitCardId("card-receiver");
    YankiPayment pending = pendingPayment();
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(sender));
    when(walletRepository.findByPhoneNumber("999333444")).thenReturn(Maybe.just(receiver));
    when(walletRepository.save(sender)).thenReturn(Single.just(sender));
    when(paymentFactory.create(incoming, sender, receiver, YankiPaymentStatus.PENDING))
            .thenReturn(pending);
    when(paymentRepository.save(pending)).thenReturn(Single.just(pending));
    when(eventPublisher.publishPaymentRequested(pending)).thenReturn(Completable.complete());

    // Act
    TestObserver<YankiPayment> observer = useCase.sendPayment(incoming).test();

    // Assert
    observer.assertComplete();
    observer.assertValue(pending);
    assertEquals(new BigDecimal("70.00"), sender.getBalance());
    verify(walletRepository).save(sender);
  }

  @Test
  @DisplayName("Should be reject a payment when the amount is null")
  void shouldBeRejectNullAmount() {
    // Arrange
    incoming.setAmount(null);

    // Act & Assert
    BusinessRuleException error = assertThrows(
            BusinessRuleException.class, () -> useCase.sendPayment(incoming));
    assertEquals("Payment amount must be greater than zero.", error.getMessage());
    verify(walletRepository, never()).findByPhoneNumber(any());
  }

  @Test
  @DisplayName("Should be reject a payment when the amount is not positive")
  void shouldBeRejectNonPositiveAmount() {
    // Arrange
    incoming.setAmount(BigDecimal.ZERO);

    // Act & Assert
    assertThrows(BusinessRuleException.class, () -> useCase.sendPayment(incoming));
    verify(walletRepository, never()).findByPhoneNumber(any());
  }

  @Test
  @DisplayName("Should be reject a payment when sender and receiver share the phone number")
  void shouldBeRejectSamePhoneNumbers() {
    // Arrange
    incoming.setReceiverPhoneNumber("999111222");

    // Act & Assert
    BusinessRuleException error = assertThrows(
            BusinessRuleException.class, () -> useCase.sendPayment(incoming));
    assertEquals("Sender and receiver phone numbers must be different.", error.getMessage());
  }

  @Test
  @DisplayName("Should be fail with WalletNotFoundException when the sender wallet is missing")
  void shouldBeFailWhenSenderWalletIsMissing() {
    // Arrange
    YankiWallet receiver = wallet("999333444", "0.00");
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.empty());
    when(walletRepository.findByPhoneNumber("999333444")).thenReturn(Maybe.just(receiver));

    // Act
    TestObserver<YankiPayment> observer = useCase.sendPayment(incoming).test();

    // Assert
    observer.assertError(WalletNotFoundException.class);
    observer.assertError(error -> error.getMessage().contains("Sender Yanki wallet"));
    verify(paymentRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be fail with BusinessRuleException when the receiver is not active")
  void shouldBeFailWhenReceiverIsNotActive() {
    // Arrange
    YankiWallet sender = wallet("999111222", "100.00");
    YankiWallet receiver = wallet("999333444", "0.00");
    receiver.setStatus(YankiWalletStatus.BLOCKED);
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(sender));
    when(walletRepository.findByPhoneNumber("999333444")).thenReturn(Maybe.just(receiver));

    // Act
    TestObserver<YankiPayment> observer = useCase.sendPayment(incoming).test();

    // Assert
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("Receiver Yanki wallet"));
  }

  @Test
  @DisplayName("Should be propagate an error when saving a wallet fails")
  void shouldBePropagateWalletSaveError() {
    // Arrange
    YankiWallet sender = wallet("999111222", "100.00");
    YankiWallet receiver = wallet("999333444", "0.00");
    RuntimeException failure = new RuntimeException("write failed");
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(sender));
    when(walletRepository.findByPhoneNumber("999333444")).thenReturn(Maybe.just(receiver));
    when(paymentFactory.create(any(), any(), any(), any())).thenReturn(completedPayment());
    when(walletRepository.save(sender)).thenReturn(Single.error(failure));

    // Act
    TestObserver<YankiPayment> observer = useCase.sendPayment(incoming).test();

    // Assert
    observer.assertError(failure);
  }

  @Test
  @DisplayName("Should be propagate an error when publishing the payment result fails")
  void shouldBePropagatePublishError() {
    // Arrange
    YankiWallet sender = wallet("999111222", "100.00");
    YankiWallet receiver = wallet("999333444", "0.00");
    YankiPayment payment = completedPayment();
    RuntimeException failure = new RuntimeException("kafka unavailable");
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(sender));
    when(walletRepository.findByPhoneNumber("999333444")).thenReturn(Maybe.just(receiver));
    when(paymentFactory.create(any(), any(), any(), any())).thenReturn(payment);
    when(walletRepository.save(sender)).thenReturn(Single.just(sender));
    when(walletRepository.save(receiver)).thenReturn(Single.just(receiver));
    when(paymentRepository.save(payment)).thenReturn(Single.just(payment));
    when(eventPublisher.publishPaymentResult(payment)).thenReturn(Completable.error(failure));

    // Act
    TestObserver<YankiPayment> observer = useCase.sendPayment(incoming).test();

    // Assert
    observer.assertError(failure);
  }

  private YankiWallet wallet(String phoneNumber, String balance) {
    return YankiWallet.builder()
            .phoneNumber(phoneNumber)
            .balance(new BigDecimal(balance))
            .status(YankiWalletStatus.ACTIVE)
            .build();
  }

  private YankiPayment completedPayment() {
    return YankiPayment.builder()
            .transactionId("TX-1")
            .status(YankiPaymentStatus.COMPLETED)
            .amount(incoming.getAmount())
            .build();
  }

  private YankiPayment pendingPayment() {
    return YankiPayment.builder()
            .transactionId("TX-2")
            .status(YankiPaymentStatus.PENDING)
            .amount(incoming.getAmount())
            .build();
  }
}
