package com.pe.pivot.yanki.application.usecase;

import com.pe.pivot.yanki.application.support.YankiWalletDomainInitializer;
import com.pe.pivot.yanki.domain.exception.DuplicateWalletException;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.port.in.CreateYankiWalletUseCase;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Pure Orchestrator Use Case for registering a new Yanki mobile wallet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateYankiWalletUseCaseImpl implements CreateYankiWalletUseCase {

  // Dependencies injected via constructor
  private final YankiWalletRepositoryPort walletRepository;
  private final YankiWalletDomainInitializer initializer;

  /**
   * Creates a new Yanki wallet after validating that the phone number,
   * IMEI, and document number are unique.
   *
   * @param wallet - the YankiWallet object containing the details of the wallet to be created
   * @return - a Single emitting the created YankiWallet object
   */
  @Override
  public Single<YankiWallet> create(YankiWallet wallet) {
    initializer.initialize(wallet);

    // Validate uniqueness of phone number, IMEI, and document number before saving
    return Single.zip(
                    walletRepository.existsByPhoneNumber(wallet.getPhoneNumber()),
                    walletRepository.existsByImei(wallet.getImei()),
                    walletRepository.existsByDocumentNumber(wallet.getDocumentNumber()),
                    // Lambda function to handle the results of the uniqueness checks
                    (phoneExists, imeiExists, docExists) -> {
                      // Throw if any uniqueness check failed
                      if (Boolean.TRUE.equals(phoneExists)) {
                        throw new DuplicateWalletException(
                                "Phone number is already registered in Yanki: "
                                        + wallet.getPhoneNumber());
                      }
                      if (Boolean.TRUE.equals(imeiExists)) {
                        throw new DuplicateWalletException(
                                "Device IMEI is already registered in Yanki.");
                      }
                      // Throw if the document number already exists
                      if (Boolean.TRUE.equals(docExists)) {
                        throw new DuplicateWalletException(
                                "Document number is already registered in Yanki.");
                      }
                      return wallet;
                    })
            // If all uniqueness checks pass, save the wallet to the repository
            .flatMap(walletRepository::save)
            .doOnSuccess(saved -> log.info("Yanki wallet created for phone: {}",
                    saved.getPhoneNumber()));
  }
}

