package com.pe.pivot.yanki.domain.port.in;

import io.reactivex.rxjava3.core.Completable;

public interface DeleteYankiWalletUseCase {

  /**
   * Deletes a Yanki wallet based on the provided phone number.
   * If the wallet does not exist, it may throw a WalletNotFoundException.
   *
   * @param phoneNumber - the phone number associated with the Yanki wallet to be deleted
   * @return - a Completable indicating the completion of the delete operation
   */
  Completable delete(String phoneNumber);
}
