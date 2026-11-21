package com.pe.pivot.yanki.infrastructure.adapter.out.persistence;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.entity.YankiPaymentDocument;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.mapper.YankiPersistenceMapper;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.repository.ReactiveYankiPaymentMongoRepository;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Unit test suite for YankiPaymentPersistenceAdapter.
 */
class YankiPaymentPersistenceAdapterTest {

  private ReactiveYankiPaymentMongoRepository repository;
  private YankiPersistenceMapper mapper;
  private YankiPaymentPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    repository = mock(ReactiveYankiPaymentMongoRepository.class);
    mapper = mock(YankiPersistenceMapper.class);
    adapter = new YankiPaymentPersistenceAdapter(repository, mapper);
  }

  @Test
  @DisplayName("Should be save a payment and return the persisted domain object (happy path)")
  void shouldBeSavePayment() {
    // Arrange
    YankiPayment domain = YankiPayment.builder().transactionId("TX-1").build();
    YankiPaymentDocument document = YankiPaymentDocument.builder()
            .transactionId("TX-1").build();
    YankiPayment saved = YankiPayment.builder().id("payment-1")
            .transactionId("TX-1").build();
    when(mapper.toPaymentDocument(domain)).thenReturn(document);
    when(repository.save(document)).thenReturn(Mono.just(document));
    when(mapper.toPaymentDomain(document)).thenReturn(saved);

    // Act
    TestObserver<YankiPayment> observer = adapter.save(domain).test();

    // Assert
    observer.assertComplete();
    observer.assertNoErrors();
    observer.assertValue(saved);
  }

  @Test
  @DisplayName("Should be find a payment by transaction id when it exists")
  void shouldBeFindPaymentByTransactionId() {
    // Arrange
    YankiPaymentDocument document = YankiPaymentDocument.builder()
            .transactionId("TX-1").build();
    YankiPayment domain = YankiPayment.builder().transactionId("TX-1").build();
    when(repository.findByTransactionId("TX-1")).thenReturn(Mono.just(document));
    when(mapper.toPaymentDomain(document)).thenReturn(domain);

    // Act
    TestObserver<YankiPayment> observer = adapter.findByTransactionId("TX-1").test();

    // Assert
    observer.assertComplete();
    observer.assertValue(domain);
  }

  @Test
  @DisplayName("Should be complete empty when the transaction id does not exist")
  void shouldBeCompleteEmptyWhenPaymentDoesNotExist() {
    // Arrange
    when(repository.findByTransactionId("TX-404")).thenReturn(Mono.empty());

    // Act
    TestObserver<YankiPayment> observer = adapter.findByTransactionId("TX-404").test();

    // Assert
    observer.assertComplete();
    observer.assertNoValues();
  }

  @Test
  @DisplayName("Should be emit every payment associated with a phone number")
  void shouldBeFindPaymentsByPhoneNumber() {
    // Arrange
    YankiPaymentDocument firstDocument = YankiPaymentDocument.builder()
            .transactionId("TX-1").build();
    YankiPaymentDocument secondDocument = YankiPaymentDocument.builder()
            .transactionId("TX-2").build();
    YankiPayment first = YankiPayment.builder().transactionId("TX-1").build();
    YankiPayment second = YankiPayment.builder().transactionId("TX-2").build();
    when(repository.findBySenderPhoneNumberOrReceiverPhoneNumber(
            "999111222", "999111222"))
            .thenReturn(Flux.just(firstDocument, secondDocument));
    when(mapper.toPaymentDomain(firstDocument)).thenReturn(first);
    when(mapper.toPaymentDomain(secondDocument)).thenReturn(second);

    // Act
    TestSubscriber<YankiPayment> subscriber =
            adapter.findByPhoneNumber("999111222").test();

    // Assert
    subscriber.assertComplete();
    subscriber.assertNoErrors();
    subscriber.assertValues(first, second);
  }

  @Test
  @DisplayName("Should be propagate an error when saving the payment fails")
  void shouldBePropagateSaveError() {
    // Arrange
    YankiPayment domain = YankiPayment.builder().transactionId("TX-1").build();
    YankiPaymentDocument document = YankiPaymentDocument.builder()
            .transactionId("TX-1").build();
    RuntimeException failure = new RuntimeException("write failed");
    when(mapper.toPaymentDocument(domain)).thenReturn(document);
    when(repository.save(document)).thenReturn(Mono.error(failure));

    // Act
    TestObserver<YankiPayment> observer = adapter.save(domain).test();

    // Assert
    observer.assertError(failure);
  }
}
