package com.pe.pivot.yanki.infrastructure.adapter.out.persistence;

import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.port.out.YankiPaymentRepositoryPort;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.mapper.YankiPersistenceMapper;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.repository.ReactiveYankiPaymentMongoRepository;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava3Adapter;

/**
 * MongoDB Persistence Adapter implementing YankiPaymentRepositoryPort with RxJava 3.
 */
@Component
@RequiredArgsConstructor
public class YankiPaymentPersistenceAdapter implements YankiPaymentRepositoryPort {

  private final ReactiveYankiPaymentMongoRepository repository;
  private final YankiPersistenceMapper mapper;

  /**
   * Saves a YankiPayment to the MongoDB database.
   *
   * @param payment - the YankiPayment domain object to be saved
   * @return - a Single emitting the saved YankiPayment domain object
   */
  @Override
  public Single<YankiPayment> save(YankiPayment payment) {
    return RxJava3Adapter.monoToSingle(
            repository.save(mapper.toPaymentDocument(payment)).map(mapper::toPaymentDomain));
  }

  /**
   * Finds a YankiPayment by its unique transaction ID.
   *
   * @param transactionId - the unique transaction ID associated with the YankiPayment
   * @return - a Maybe emitting the found YankiPayment or empty if not found
   */
  @Override
  public Maybe<YankiPayment> findByTransactionId(String transactionId) {
    return RxJava3Adapter.monoToMaybe(
            repository.findByTransactionId(transactionId).map(mapper::toPaymentDomain));
  }

  /**
   * Finds all YankiPayments associated with a given phone number, either as sender or receiver.
   *
   * @param phoneNumber - the phone number associated with the YankiPayments
   * @return - a Flowable emitting all YankiPayment domain objects for the specified phone number
   */
  @Override
  public Flowable<YankiPayment> findByPhoneNumber(String phoneNumber) {
    return RxJava3Adapter.fluxToFlowable(
            repository.findBySenderPhoneNumberOrReceiverPhoneNumber(phoneNumber, phoneNumber)
                    .map(mapper::toPaymentDomain));
  }
}