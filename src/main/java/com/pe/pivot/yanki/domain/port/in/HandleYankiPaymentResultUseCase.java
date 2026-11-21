package com.pe.pivot.yanki.domain.port.in;

import com.pe.pivot.yanki.domain.model.YankiPaymentResultCommand;
import io.reactivex.rxjava3.core.Completable;

/**
 * Use case interface for handling asynchronous Yanki payment results.
 */
public interface HandleYankiPaymentResultUseCase {

  /**
   * Handles the result of a Yanki payment operation and returns a Completable
   * indicating completion or error.
   *
   * @param command - the command containing the details of the payment result to handle
   * @return - a Completable that completes when the result is handled successfully
   */
  Completable handleResult(YankiPaymentResultCommand command);
}
