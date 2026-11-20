package com.pe.pivot.yanki.domain.port.in;

import com.pe.pivot.yanki.domain.model.YankiPayment;
import io.reactivex.rxjava3.core.Single;

/**
 * Use case interface for sending Yanki payments.
 * Implementations of this interface handle the business logic for processing and
 * sending payments within the Yanki domain.
 */
public interface SendYankiPaymentUseCase {

  /**
   * Sends a Yanki payment and returns the resulting YankiPayment object.
   *
   * @param payment - the YankiPayment object containing payment details to be sent
   * @return - a Single emitting the resulting YankiPayment after processing
   */
  Single<YankiPayment> sendPayment(YankiPayment payment);
}
