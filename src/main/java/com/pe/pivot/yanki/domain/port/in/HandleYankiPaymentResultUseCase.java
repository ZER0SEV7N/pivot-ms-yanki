package com.pe.pivot.yanki.domain.port.in;

import com.pe.pivot.yanki.domain.model.YankiPaymentResultCommand;
import io.reactivex.rxjava3.core.Completable;

public interface HandleYankiPaymentResultUseCase {

  /**
   * Handles the result of a Yanki payment operation,
   * processing the provided command and returning a Completable to indicate completion or error.
   *
   * @param command - the YankiPaymentResultCommand containing the details of the payment result to be handled
   * @return - a Completable that completes when the payment result has been successfully handled, or emits an error if the handling fails
   */
  Completable handleResult(YankiPaymentResultCommand command);
}
