package com.pe.pivot.yanki.domain.port.out;

import com.pe.pivot.yanki.domain.model.YankiPayment;
import io.reactivex.rxjava3.core.Completable;

/**
 * Output port for publishing Yanki payment events to Apache Kafka.
 */
public interface YankiEventPublisherPort {

  /**
   * Publishes a Yanki payment request event to Apache Kafka,
   * indicating that a payment has been initiated.
   *
   * @param payment - the YankiPayment object containing the payment details
   * @return - a Completable indicating the completion of the event publishing
   */
  Completable publishPaymentRequested(YankiPayment payment);

  /**
   * Publishes the result of a Yanki payment to Apache Kafka,
   * indicating whether the payment was completed or failed.
   *
   * @param payment - the YankiPayment object containing the payment details and status
   * @return - a Completable indicating the completion of the event publishing
   */
  Completable publishPaymentResult(YankiPayment payment);
}