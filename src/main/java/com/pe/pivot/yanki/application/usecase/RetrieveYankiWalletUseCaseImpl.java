package com.pe.pivot.yanki.application.usecase;

import com.pe.pivot.yanki.domain.exception.WalletNotFoundException;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.port.in.RetrieveYankiWalletUseCase;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use Case for retrieving Yanki wallets (accelerated via Redis Decorator by phoneNumber).
 */
@Service
@RequiredArgsConstructor
public class RetrieveYankiWalletUseCaseImpl implements RetrieveYankiWalletUseCase {

  private final YankiWalletRepositoryPort walletRepository;

  /**
   * Retrieves all Yanki wallets from the repository.
   *
   * @return - a Flowable emitting all YankiWallet objects
   */
  @Override
  public Flowable<YankiWallet> findAll() {
    return walletRepository.findAll();
  }

  /**
   * Retrieves a Yanki wallet by its unique identifier.
   *
   * @param phoneNumber - the unique phone number associated with the Yanki wallet
   * @return - a Single emitting the found YankiWallet or an error if not found
   */
  @Override
  public Single<YankiWallet> findByPhoneNumber(String phoneNumber) {
    return walletRepository.findByPhoneNumber(phoneNumber)
            .switchIfEmpty(Single.error(new WalletNotFoundException(
                    "Yanki wallet not found for phone number: " + phoneNumber)));
  }
}