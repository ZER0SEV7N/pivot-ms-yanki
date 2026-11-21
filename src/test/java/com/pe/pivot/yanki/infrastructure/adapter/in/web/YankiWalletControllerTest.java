package com.pe.pivot.yanki.infrastructure.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.yanki.domain.exception.DuplicateWalletException;
import com.pe.pivot.yanki.domain.exception.WalletNotFoundException;
import com.pe.pivot.yanki.domain.model.YankiDocumentType;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.model.YankiWalletStatus;
import com.pe.pivot.yanki.domain.port.in.CreateYankiWalletUseCase;
import com.pe.pivot.yanki.domain.port.in.DeleteYankiWalletUseCase;
import com.pe.pivot.yanki.domain.port.in.RetrieveYankiWalletUseCase;
import com.pe.pivot.yanki.domain.port.in.UpdateYankiWalletUseCase;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.LinkDebitCardRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.TopUpRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiWalletRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiWalletResponse;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiWalletUpdateRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.mapper.YankiWebMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for YankiWalletController.
 */
class YankiWalletControllerTest {

  private CreateYankiWalletUseCase createWalletUseCase;
  private RetrieveYankiWalletUseCase retrieveWalletUseCase;
  private UpdateYankiWalletUseCase updateWalletUseCase;
  private DeleteYankiWalletUseCase deleteWalletUseCase;
  private YankiWalletController controller;
  private YankiWallet wallet;

  @BeforeEach
  void setUp() {
    createWalletUseCase = mock(CreateYankiWalletUseCase.class);
    retrieveWalletUseCase = mock(RetrieveYankiWalletUseCase.class);
    updateWalletUseCase = mock(UpdateYankiWalletUseCase.class);
    deleteWalletUseCase = mock(DeleteYankiWalletUseCase.class);
    controller = new YankiWalletController(createWalletUseCase, retrieveWalletUseCase,
            updateWalletUseCase, deleteWalletUseCase, new YankiWebMapper());
    wallet = YankiWallet.builder()
            .id("wallet-1")
            .documentType(YankiDocumentType.DNI)
            .documentNumber("12345678")
            .phoneNumber("999111222")
            .imei("IMEI-1")
            .email("john@mail.com")
            .balance(BigDecimal.ZERO)
            .status(YankiWalletStatus.ACTIVE)
            .build();
  }

  @Test
  @DisplayName("Should be create a wallet and return its response (happy path)")
  void shouldBeCreateWallet() {
    // Arrange
    YankiWalletRequest request = YankiWalletRequest.builder()
            .documentType(YankiDocumentType.DNI)
            .documentNumber("12345678")
            .phoneNumber("999111222")
            .imei("IMEI-1")
            .email("john@mail.com")
            .build();
    when(createWalletUseCase.create(any(YankiWallet.class))).thenReturn(Single.just(wallet));

    // Act
    TestObserver<YankiWalletResponse> observer =
            controller.createWallet(request).test();

    // Assert
    observer.assertComplete();
    observer.assertValue(response -> {
      assertEquals("wallet-1", response.id());
      assertEquals("999111222", response.phoneNumber());
      assertEquals(YankiWalletStatus.ACTIVE, response.status());
      return true;
    });
    verify(createWalletUseCase).create(argThat(domain ->
            "12345678".equals(domain.getDocumentNumber())));
  }

  @Test
  @DisplayName("Should be return every wallet as a response")
  void shouldBeGetAllWallets() {
    // Arrange
    YankiWallet second = YankiWallet.builder()
            .id("wallet-2")
            .phoneNumber("999333444")
            .build();
    when(retrieveWalletUseCase.findAll()).thenReturn(Flowable.just(wallet, second));

    // Act
    TestSubscriber<YankiWalletResponse> subscriber =
            controller.getAllWallets().test();

    // Assert
    subscriber.assertComplete();
    subscriber.assertValueCount(2);
    subscriber.assertValueAt(0, response ->
            "999111222".equals(response.phoneNumber()));
    subscriber.assertValueAt(1, response ->
            "999333444".equals(response.phoneNumber()));
  }

  @Test
  @DisplayName("Should be return a wallet by phone number")
  void shouldBeGetWalletByPhoneNumber() {
    // Arrange
    when(retrieveWalletUseCase.findByPhoneNumber("999111222"))
            .thenReturn(Single.just(wallet));

    // Act
    TestObserver<YankiWalletResponse> observer =
            controller.getWalletByPhone("999111222").test();

    // Assert
    observer.assertComplete();
    observer.assertValue(response -> "wallet-1".equals(response.id()));
    verify(retrieveWalletUseCase).findByPhoneNumber("999111222");
  }

  @Test
  @DisplayName("Should be update a wallet from the request payload")
  void shouldBeUpdateWallet() {
    // Arrange
    YankiWalletUpdateRequest request = YankiWalletUpdateRequest.builder()
            .email("new@mail.com")
            .build();
    when(updateWalletUseCase.update(eq("999111222"), any(YankiWallet.class)))
            .thenReturn(Single.just(wallet));

    // Act
    TestObserver<YankiWalletResponse> observer =
            controller.updateWallet("999111222", request).test();

    // Assert
    observer.assertComplete();
    observer.assertValue(response -> "wallet-1".equals(response.id()));
    verify(updateWalletUseCase).update(eq("999111222"), any(YankiWallet.class));
  }

  @Test
  @DisplayName("Should be link a debit card to the wallet")
  void shouldBeLinkDebitCard() {
    // Arrange
    LinkDebitCardRequest request = LinkDebitCardRequest.builder()
            .debitCardId("card-1")
            .build();
    wallet.setLinkedDebitCardId("card-1");
    when(updateWalletUseCase.linkDebitCard("999111222", "card-1"))
            .thenReturn(Single.just(wallet));

    // Act
    TestObserver<YankiWalletResponse> observer =
            controller.linkDebitCard("999111222", request).test();

    // Assert
    observer.assertComplete();
    observer.assertValue(response -> "card-1".equals(response.linkedDebitCardId()));
  }

  @Test
  @DisplayName("Should be top up the wallet balance")
  void shouldBeTopUpWallet() {
    // Arrange
    TopUpRequest request = TopUpRequest.builder()
            .amount(new BigDecimal("25.00"))
            .build();
    wallet.setBalance(new BigDecimal("25.00"));
    when(updateWalletUseCase.topUp("999111222", new BigDecimal("25.00")))
            .thenReturn(Single.just(wallet));

    // Act
    TestObserver<YankiWalletResponse> observer =
            controller.topUpWallet("999111222", request).test();

    // Assert
    observer.assertComplete();
    observer.assertValue(response ->
            new BigDecimal("25.00").equals(response.balance()));
  }

  @Test
  @DisplayName("Should be delete a wallet returning an empty completion")
  void shouldBeDeleteWallet() {
    // Arrange
    when(deleteWalletUseCase.delete("999111222")).thenReturn(Completable.complete());

    // Act
    TestObserver<Void> observer = controller.deleteWallet("999111222").test();

    // Assert
    observer.assertComplete();
    observer.assertNoErrors();
    verify(deleteWalletUseCase).delete("999111222");
  }

  @Test
  @DisplayName("Should be propagate DuplicateWalletException when creation conflicts")
  void shouldBePropagateCreateError() {
    // Arrange
    YankiWalletRequest request = YankiWalletRequest.builder()
            .documentType(YankiDocumentType.DNI)
            .documentNumber("12345678")
            .phoneNumber("999111222")
            .imei("IMEI-1")
            .email("john@mail.com")
            .build();
    when(createWalletUseCase.create(any(YankiWallet.class)))
            .thenReturn(Single.error(new DuplicateWalletException("Duplicate wallet")));

    // Act
    TestObserver<YankiWalletResponse> observer =
            controller.createWallet(request).test();

    // Assert
    observer.assertError(DuplicateWalletException.class);
    observer.assertNoValues();
  }

  @Test
  @DisplayName("Should be propagate WalletNotFoundException when the wallet is missing")
  void shouldBePropagateNotFoundError() {
    // Arrange
    when(retrieveWalletUseCase.findByPhoneNumber("999000000"))
            .thenReturn(Single.error(new WalletNotFoundException("Wallet not found")));

    // Act
    TestObserver<YankiWalletResponse> observer =
            controller.getWalletByPhone("999000000").test();

    // Assert
    observer.assertError(WalletNotFoundException.class);
  }
}
