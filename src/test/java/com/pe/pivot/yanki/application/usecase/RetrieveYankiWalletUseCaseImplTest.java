package com.pe.pivot.yanki.application.usecase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.yanki.domain.exception.WalletNotFoundException;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for RetrieveYankiWalletUseCaseImpl.
 */
class RetrieveYankiWalletUseCaseImplTest {

  private YankiWalletRepositoryPort walletRepository;
  private RetrieveYankiWalletUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    walletRepository = mock(YankiWalletRepositoryPort.class);
    useCase = new RetrieveYankiWalletUseCaseImpl(walletRepository);
  }

  @Test
  @DisplayName("Should be emit every wallet from the repository (happy path)")
  void shouldBeEmitEveryWallet() {
    // Arrange
    YankiWallet first = YankiWallet.builder().phoneNumber("999111222").build();
    YankiWallet second = YankiWallet.builder().phoneNumber("999333444").build();
    when(walletRepository.findAll()).thenReturn(Flowable.just(first, second));

    // Act
    TestSubscriber<YankiWallet> subscriber = useCase.findAll().test();

    // Assert
    subscriber.assertComplete();
    subscriber.assertNoErrors();
    subscriber.assertValues(first, second);
  }

  @Test
  @DisplayName("Should be find a wallet by its phone number")
  void shouldBeFindWalletByPhoneNumber() {
    // Arrange
    YankiWallet wallet = YankiWallet.builder().phoneNumber("999111222").build();
    when(walletRepository.findByPhoneNumber("999111222")).thenReturn(Maybe.just(wallet));

    // Act
    TestObserver<YankiWallet> observer = useCase.findByPhoneNumber("999111222").test();

    // Assert
    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertValue(wallet);
    verify(walletRepository).findByPhoneNumber("999111222");
  }

  @Test
  @DisplayName("Should be fail with WalletNotFoundException when the wallet is missing")
  void shouldBeFailWhenWalletIsMissing() {
    // Arrange
    when(walletRepository.findByPhoneNumber("999000000")).thenReturn(Maybe.empty());

    // Act
    TestObserver<YankiWallet> observer = useCase.findByPhoneNumber("999000000").test();

    // Assert
    observer.assertError(WalletNotFoundException.class);
    observer.assertError(error -> error.getMessage().contains("999000000"));
    observer.assertNoValues();
  }
}
