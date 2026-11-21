package com.pe.pivot.yanki.infrastructure.adapter.out.persistence;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.entity.YankiWalletDocument;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.mapper.YankiPersistenceMapper;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.repository.ReactiveYankiWalletMongoRepository;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Unit test suite for YankiWalletPersistenceAdapter.
 */
class YankiWalletPersistenceAdapterTest {

  private ReactiveYankiWalletMongoRepository repository;
  private YankiPersistenceMapper mapper;
  private YankiWalletPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    repository = mock(ReactiveYankiWalletMongoRepository.class);
    mapper = mock(YankiPersistenceMapper.class);
    adapter = new YankiWalletPersistenceAdapter(repository, mapper);
  }

  @Test
  @DisplayName("Should be save a wallet and return the persisted domain object (happy path)")
  void shouldBeSaveWallet() {
    // Arrange
    YankiWallet domain = YankiWallet.builder().phoneNumber("999111222").build();
    YankiWalletDocument document = YankiWalletDocument.builder()
            .phoneNumber("999111222").build();
    YankiWallet saved = YankiWallet.builder().id("wallet-1")
            .phoneNumber("999111222").build();
    when(mapper.toWalletDocument(domain)).thenReturn(document);
    when(repository.save(document)).thenReturn(Mono.just(document));
    when(mapper.toWalletDomain(document)).thenReturn(saved);

    // Act
    TestObserver<YankiWallet> observer = adapter.save(domain).test();

    // Assert
    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertValue(saved);
    verify(repository).save(document);
  }

  @Test
  @DisplayName("Should be emit every wallet from MongoDB")
  void shouldBeFindAllWallets() {
    // Arrange
    YankiWalletDocument firstDocument = YankiWalletDocument.builder()
            .phoneNumber("999111222").build();
    YankiWalletDocument secondDocument = YankiWalletDocument.builder()
            .phoneNumber("999333444").build();
    YankiWallet first = YankiWallet.builder().phoneNumber("999111222").build();
    YankiWallet second = YankiWallet.builder().phoneNumber("999333444").build();
    when(repository.findAll()).thenReturn(Flux.just(firstDocument, secondDocument));
    when(mapper.toWalletDomain(firstDocument)).thenReturn(first);
    when(mapper.toWalletDomain(secondDocument)).thenReturn(second);

    // Act
    TestSubscriber<YankiWallet> subscriber = adapter.findAll().test();

    // Assert
    subscriber.assertComplete();
    subscriber.assertNoErrors();
    subscriber.assertValues(first, second);
  }

  @Test
  @DisplayName("Should be find a wallet by phone number when it exists")
  void shouldBeFindWalletByPhoneNumber() {
    // Arrange
    YankiWalletDocument document = YankiWalletDocument.builder()
            .phoneNumber("999111222").build();
    YankiWallet domain = YankiWallet.builder().phoneNumber("999111222").build();
    when(repository.findByPhoneNumber("999111222")).thenReturn(Mono.just(document));
    when(mapper.toWalletDomain(document)).thenReturn(domain);

    // Act
    TestObserver<YankiWallet> observer = adapter.findByPhoneNumber("999111222").test();

    // Assert
    observer.assertComplete();
    observer.assertValue(domain);
  }

  @Test
  @DisplayName("Should be complete empty when the phone number does not exist")
  void shouldBeCompleteEmptyWhenWalletDoesNotExist() {
    // Arrange
    when(repository.findByPhoneNumber("999000000")).thenReturn(Mono.empty());

    // Act
    TestObserver<YankiWallet> observer = adapter.findByPhoneNumber("999000000").test();

    // Assert
    observer.assertComplete();
    observer.assertNoValues();
  }

  @Test
  @DisplayName("Should be report existence by phone number, IMEI, and document number")
  void shouldBeReportExistenceChecks() {
    // Arrange
    when(repository.existsByPhoneNumber("999111222")).thenReturn(Mono.just(true));
    when(repository.existsByImei("IMEI-1")).thenReturn(Mono.just(true));
    when(repository.existsByDocumentNumber("12345678")).thenReturn(Mono.just(false));

    // Act
    TestObserver<Boolean> phone = adapter.existsByPhoneNumber("999111222").test();
    TestObserver<Boolean> imei = adapter.existsByImei("IMEI-1").test();
    TestObserver<Boolean> document = adapter.existsByDocumentNumber("12345678").test();

    // Assert
    phone.assertValue(true);
    imei.assertValue(true);
    document.assertValue(false);
  }

  @Test
  @DisplayName("Should be propagate an error when saving the wallet fails")
  void shouldBePropagateSaveError() {
    // Arrange
    YankiWallet domain = YankiWallet.builder().phoneNumber("999111222").build();
    YankiWalletDocument document = YankiWalletDocument.builder()
            .phoneNumber("999111222").build();
    RuntimeException failure = new RuntimeException("write failed");
    when(mapper.toWalletDocument(domain)).thenReturn(document);
    when(repository.save(document)).thenReturn(Mono.error(failure));

    // Act
    TestObserver<YankiWallet> observer = adapter.save(domain).test();

    // Assert
    observer.assertError(failure);
  }
}
