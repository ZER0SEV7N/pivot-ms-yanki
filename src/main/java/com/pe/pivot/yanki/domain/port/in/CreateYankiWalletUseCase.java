package com.pe.pivot.yanki.domain.port.in;

import com.pe.pivot.yanki.domain.model.YankiWallet;
import io.reactivex.rxjava3.core.Single;

/**
 * Use case interface for creating a new Yanki wallet. This interface defines the contract for
 */
public interface CreateYankiWalletUseCase {

  /**
   * Creates a new Yanki wallet in the system. The implementation of this method should handle
   *
   * @param wallet - the YankiWallet object containing the details of the wallet to be created
   * @return - a Single emitting the created YankiWallet object upon successful creation
   */
  Single<YankiWallet> create(YankiWallet wallet);
}
