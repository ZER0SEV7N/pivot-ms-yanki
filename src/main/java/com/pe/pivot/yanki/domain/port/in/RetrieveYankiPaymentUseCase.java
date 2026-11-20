package com.pe.pivot.yanki.domain.port.in;

import com.pe.pivot.yanki.domain.model.YankiPayment;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Use case interface for retrieving Yanki payment information.
 * Provides methods to find payments by transaction ID and by phone number.
 */
public interface RetrieveYankiPaymentUseCase {

  /**
   * Finds a Yanki payment by its associated transaction ID.
   *
   * @param transactionId - the unique identifier of the transaction associated with the payment
   * @return - a Single emitting the found YankiPayment or an error if not found
   */
  Single<YankiPayment> findByTransactionId(String transactionId);

  /**
   * Finds Yanki payments associated with a specific phone number.
   *
   * @param phoneNumber - the phone number associated with the Yanki payments
   * @return - a Flowable emitting all YankiPayment records associated with the given phone number
   */
  Flowable<YankiPayment> findByPhoneNumber(String phoneNumber);
}
