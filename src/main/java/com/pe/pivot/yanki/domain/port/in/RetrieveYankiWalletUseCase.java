package com.pe.pivot.yanki.domain.port.in;

import com.pe.pivot.yanki.domain.model.YankiWallet;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Use case interface for retrieving Yanki wallets.
 */
public interface RetrieveYankiWalletUseCase {

  Flowable<YankiWallet> findAll();

  Single<YankiWallet> findByPhoneNumber(String phoneNumber);
}
