package com.pe.pivot.yanki.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.yanki.application.support.YankiWalletDomainInitializer;
import com.pe.pivot.yanki.domain.exception.DuplicateWalletException;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for CreateYankiWalletUseCaseImpl.
 */
class CreateYankiWalletUseCaseImplTest {

  private YankiWalletRepositoryPort walletRepository;
  private YankiWalletDomainInitializer initializer;
  private CreateYankiWalletUseCaseImpl useCase;
  private YankiWallet wallet;

  @BeforeEach
  void setUp() {
    walletRepository = mock(YankiWalletRepositoryPort.class);
    initializer = mock(YankiWalletDomainInitializer.class);
    useCase = new CreateYankiWalletUseCaseImpl(walletRepository, initializer);
    wallet = YankiWallet.builder()
            .documentNumber("12345678")
            .phoneNumber("999111222")
            .imei("IMEI-1")
            .balance(BigDecimal.ZERO)
            .build();
  }

  @Test
  @DisplayName("Should be create a wallet when all unique fields are free (happy path)")
  void shouldBeCreateWalletWhenFieldsAreUnique() {
    // Arrange
    when(walletRepository.existsByPhoneNumber("999111222")).thenReturn(Single.just(false));
    when(walletRepository.existsByImei("IMEI-1")).thenReturn(Single.just(false));
    when(walletRepository.existsByDocumentNumber("12345678")).thenReturn(Single.just(false));
    when(walletRepository.save(wallet)).thenReturn(Single.just(wallet));

    // Act
    TestObserver<YankiWallet> observer = useCase.create(wallet).test();

    // Assert
    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertValue(wallet);
    verify(initializer).initialize(wallet);
    verify(walletRepository).save(wallet);
  }

  @Test
  @DisplayName("Should be reject a wallet when the phone number already exists")
  void shouldBeRejectDuplicatePhoneNumber() {
    // Arrange
    when(walletRepository.existsByPhoneNumber("999111222")).thenReturn(Single.just(true));
    when(walletRepository.existsByImei("IMEI-1")).thenReturn(Single.just(false));
    when(walletRepository.existsByDocumentNumber("12345678")).thenReturn(Single.just(false));

    // Act
    TestObserver<YankiWallet> observer = useCase.create(wallet).test();

    // Assert
    observer.assertError(DuplicateWalletException.class);
    observer.assertError(error -> error.getMessage().contains("Phone number"));
    verify(walletRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be reject a wallet when the IMEI already exists")
  void shouldBeRejectDuplicateImei() {
    // Arrange
    when(walletRepository.existsByPhoneNumber("999111222")).thenReturn(Single.just(false));
    when(walletRepository.existsByImei("IMEI-1")).thenReturn(Single.just(true));
    when(walletRepository.existsByDocumentNumber("12345678")).thenReturn(Single.just(false));

    // Act
    TestObserver<YankiWallet> observer = useCase.create(wallet).test();

    // Assert
    observer.assertError(error -> error instanceof DuplicateWalletException
            && error.getMessage().contains("IMEI"));
    verify(walletRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be reject a wallet when the document number already exists")
  void shouldBeRejectDuplicateDocumentNumber() {
    // Arrange
    when(walletRepository.existsByPhoneNumber("999111222")).thenReturn(Single.just(false));
    when(walletRepository.existsByImei("IMEI-1")).thenReturn(Single.just(false));
    when(walletRepository.existsByDocumentNumber("12345678")).thenReturn(Single.just(true));

    // Act
    TestObserver<YankiWallet> observer = useCase.create(wallet).test();

    // Assert
    observer.assertError(error -> error instanceof DuplicateWalletException
            && error.getMessage().contains("Document number"));
    verify(walletRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be propagate a repository error during the uniqueness checks")
  void shouldBePropagateUniquenessCheckError() {
    // Arrange
    RuntimeException failure = new RuntimeException("mongo unavailable");
    when(walletRepository.existsByPhoneNumber("999111222")).thenReturn(Single.error(failure));
    when(walletRepository.existsByImei("IMEI-1")).thenReturn(Single.just(false));
    when(walletRepository.existsByDocumentNumber("12345678")).thenReturn(Single.just(false));

    // Act
    TestObserver<YankiWallet> observer = useCase.create(wallet).test();

    // Assert
    observer.assertError(failure);
    verify(walletRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should be propagate an error when saving the wallet fails")
  void shouldBePropagateSaveError() {
    // Arrange
    RuntimeException failure = new RuntimeException("write failed");
    when(walletRepository.existsByPhoneNumber("999111222")).thenReturn(Single.just(false));
    when(walletRepository.existsByImei("IMEI-1")).thenReturn(Single.just(false));
    when(walletRepository.existsByDocumentNumber("12345678")).thenReturn(Single.just(false));
    when(walletRepository.save(wallet)).thenReturn(Single.error(failure));

    // Act
    TestObserver<YankiWallet> observer = useCase.create(wallet).test();

    // Assert
    observer.assertError(failure);
  }
}
