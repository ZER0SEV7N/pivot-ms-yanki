package com.pe.pivot.yanki.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.yanki.domain.exception.BusinessRuleException;
import com.pe.pivot.yanki.domain.exception.WalletNotFoundException;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.model.YankiWalletStatus;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for UpdateYankiWalletUseCaseImpl.
 */
class UpdateYankiWalletUseCaseImplTest {

  private YankiWalletRepositoryPort walletRepository;
  private UpdateYankiWalletUseCaseImpl useCase;
  private YankiWallet existing;

  @BeforeEach
  void setUp() {
    walletRepository = mock(YankiWalletRepositoryPort.class);
    useCase = new UpdateYankiWalletUseCaseImpl(walletRepository);
    existing = YankiWallet.builder()
            .id("wallet-1")
            .phoneNumber("999111222")
            .email("old@mail.com")
            .imei("OLD-IMEI")
            .balance(new BigDecimal("10.00"))
            .status(YankiWalletStatus.ACTIVE)
            .build();
  }

  @Test
  @DisplayName("Should be update the wallet metadata with normalized values (happy path)")
  void shouldBeUpdateWalletMetadata() {
    // Arrange
    YankiWallet incoming = YankiWallet.builder()
            .email("  NEW@MAIL.COM  ")
            .imei("  NEW-IMEI  ")
            .status(YankiWalletStatus.BLOCKED)
            .build();
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(existing));
    when(walletRepository.save(existing)).thenReturn(Single.just(existing));

    // Act
    TestObserver<YankiWallet> observer = useCase.update("999111222", incoming).test();

    // Assert
    observer.assertComplete();
    observer.assertValue(existing);
    assertEquals("new@mail.com", existing.getEmail());
    assertEquals("NEW-IMEI", existing.getImei());
    assertEquals(YankiWalletStatus.BLOCKED, existing.getStatus());
    assertNotNull(existing.getUpdatedAt());
    verify(walletRepository).save(existing);
  }

  @Test
  @DisplayName("Should be keep existing values when the update payload has null fields")
  void shouldBeKeepValuesWhenPayloadFieldsAreNull() {
    // Arrange
    YankiWallet incoming = new YankiWallet();
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(existing));
    when(walletRepository.save(existing)).thenReturn(Single.just(existing));

    // Act
    TestObserver<YankiWallet> observer = useCase.update("999111222", incoming).test();

    // Assert
    observer.assertComplete();
    assertEquals("old@mail.com", existing.getEmail());
    assertEquals("OLD-IMEI", existing.getImei());
    assertEquals(YankiWalletStatus.ACTIVE, existing.getStatus());
  }

  @Test
  @DisplayName("Should be link a debit card to an active wallet")
  void shouldBeLinkDebitCard() {
    // Arrange
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(existing));
    when(walletRepository.save(existing)).thenReturn(Single.just(existing));

    // Act
    TestObserver<YankiWallet> observer =
            useCase.linkDebitCard("999111222", "  card-1  ").test();

    // Assert
    observer.assertComplete();
    assertEquals("card-1", existing.getLinkedDebitCardId());
    assertNotNull(existing.getUpdatedAt());
    verify(walletRepository).save(existing);
  }

  @Test
  @DisplayName("Should be fail with WalletNotFoundException when linking a card to a "
          + "missing wallet")
  void shouldBeFailLinkWhenWalletDoesNotExist() {
    // Arrange
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.empty());

    // Act
    TestObserver<YankiWallet> observer =
            useCase.linkDebitCard("999111222", "card-1").test();

    // Assert
    observer.assertError(WalletNotFoundException.class);
    verify(walletRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be reject any update on a CLOSED wallet")
  void shouldBeRejectUpdateWhenWalletIsClosed() {
    // Arrange
    existing.setStatus(YankiWalletStatus.CLOSED);
    YankiWallet incoming = YankiWallet.builder().email("new@mail.com").build();
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(existing));

    // Act
    TestObserver<YankiWallet> observer = useCase.update("999111222", incoming).test();

    // Assert
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("CLOSED"));
    verify(walletRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be top up the wallet balance")
  void shouldBeTopUpWallet() {
    // Arrange
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(existing));
    when(walletRepository.save(existing)).thenReturn(Single.just(existing));

    // Act
    TestObserver<YankiWallet> observer =
            useCase.topUp("999111222", new BigDecimal("5.50")).test();

    // Assert
    observer.assertComplete();
    assertEquals(new BigDecimal("15.50"), existing.getBalance());
    verify(walletRepository).save(existing);
  }

  @Test
  @DisplayName("Should be reject a top-up when the amount is null")
  void shouldBeRejectNullTopUp() {
    // Act
    TestObserver<YankiWallet> observer = useCase.topUp("999111222", null).test();

    // Assert
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("greater than zero"));
    verify(walletRepository, never()).findByPhoneNumber(any());
  }

  @Test
  @DisplayName("Should be reject a top-up when the amount is not positive")
  void shouldBeRejectNonPositiveTopUp() {
    // Act
    TestObserver<YankiWallet> observer =
            useCase.topUp("999111222", BigDecimal.ZERO).test();

    // Assert
    observer.assertError(BusinessRuleException.class);
    verify(walletRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be fail with WalletNotFoundException when topping up a missing wallet")
  void shouldBeFailTopUpWhenWalletDoesNotExist() {
    // Arrange
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.empty());

    // Act
    TestObserver<YankiWallet> observer =
            useCase.topUp("999111222", new BigDecimal("5.00")).test();

    // Assert
    observer.assertError(WalletNotFoundException.class);
  }

  @Test
  @DisplayName("Should be propagate an error when saving the updated wallet fails")
  void shouldBePropagateSaveError() {
    // Arrange
    YankiWallet incoming = YankiWallet.builder().email("new@mail.com").build();
    RuntimeException failure = new RuntimeException("write failed");
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(existing));
    when(walletRepository.save(existing)).thenReturn(Single.error(failure));

    // Act
    TestObserver<YankiWallet> observer = useCase.update("999111222", incoming).test();

    // Assert
    observer.assertError(failure);
  }
}
