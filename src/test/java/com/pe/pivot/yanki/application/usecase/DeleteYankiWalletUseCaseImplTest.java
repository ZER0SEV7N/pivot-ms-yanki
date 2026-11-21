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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for DeleteYankiWalletUseCaseImpl.
 */
class DeleteYankiWalletUseCaseImplTest {

  private YankiWalletRepositoryPort walletRepository;
  private DeleteYankiWalletUseCaseImpl useCase;
  private YankiWallet wallet;

  @BeforeEach
  void setUp() {
    walletRepository = mock(YankiWalletRepositoryPort.class);
    useCase = new DeleteYankiWalletUseCaseImpl(walletRepository);
    wallet = YankiWallet.builder()
            .phoneNumber("999111222")
            .status(YankiWalletStatus.ACTIVE)
            .build();
  }

  @Test
  @DisplayName("Should be soft delete an active wallet by closing it (happy path)")
  void shouldBeSoftDeleteActiveWallet() {
    // Arrange
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(wallet));
    when(walletRepository.save(wallet)).thenReturn(Single.just(wallet));

    // Act
    TestObserver<Void> observer = useCase.delete("999111222").test();

    // Assert
    observer.assertComplete();
    observer.assertNoErrors();
    assertEqualsStatusClosed(wallet);
    verify(walletRepository).save(wallet);
  }

  @Test
  @DisplayName("Should be fail with WalletNotFoundException when the wallet does not exist")
  void shouldBeFailWhenWalletDoesNotExist() {
    // Arrange
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.empty());

    // Act
    TestObserver<Void> observer = useCase.delete("999111222").test();

    // Assert
    observer.assertError(WalletNotFoundException.class);
    observer.assertError(error -> error.getMessage().contains("999111222"));
    verify(walletRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be fail with BusinessRuleException when the wallet is already closed")
  void shouldBeFailWhenWalletIsAlreadyClosed() {
    // Arrange
    wallet.setStatus(YankiWalletStatus.CLOSED);
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(wallet));

    // Act
    TestObserver<Void> observer = useCase.delete("999111222").test();

    // Assert
    observer.assertError(BusinessRuleException.class);
    observer.assertError(error -> error.getMessage().contains("already CLOSED"));
    verify(walletRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be propagate an error when saving the closed wallet fails")
  void shouldBePropagateSaveError() {
    // Arrange
    RuntimeException failure = new RuntimeException("write failed");
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(wallet));
    when(walletRepository.save(wallet)).thenReturn(Single.error(failure));

    // Act
    TestObserver<Void> observer = useCase.delete("999111222").test();

    // Assert
    observer.assertError(failure);
  }

  private void assertEqualsStatusClosed(YankiWallet target) {
    assertEquals(YankiWalletStatus.CLOSED, target.getStatus());
    assertNotNull(target.getUpdatedAt());
  }
}
