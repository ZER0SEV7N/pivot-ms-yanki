package com.pe.pivot.yanki.domain.port.in;

import com.pe.pivot.yanki.domain.model.YankiWallet;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface RetrieveYankiWalletUseCase {

  Flowable<YankiWallet> findAll();

  Single<YankiWallet> findByPhoneNumber(String phoneNumber);
}
