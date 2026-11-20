package com.pe.pivot.yanki.domain.port.out;

import com.pe.pivot.yanki.domain.model.YankiPayment;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Output port for persisting and querying YankiPayment records.
 */
public interface YankiPaymentRepositoryPort {

  /**
   * Saves a YankiPayment to the repository.
   *
   * @param payment - the YankiPayment domain object to be saved
   * @return - a Single emitting the saved YankiPayment domain object
   */
  Single<YankiPayment> save(YankiPayment payment);

  /**
   * Finds a YankiPayment by its unique transaction ID.
   *
   * @param transactionId - the unique transaction ID associated with the YankiPayment
   * @return - a Maybe emitting the found YankiPayment or empty if not found
   */
  Maybe<YankiPayment> findByTransactionId(String transactionId);

  /**
   * Finds all YankiPayment records associated with a specific phone number.
   *
   * @param phoneNumber - the phone number associated with the YankiPayments
   * @return - a Flowable emitting all YankiPayment domain objects associated with the given phone number
   */
  Flowable<YankiPayment> findByPhoneNumber(String phoneNumber);
}