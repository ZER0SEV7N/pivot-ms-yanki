package com.pe.pivot.yanki.application.usecase;

import com.pe.pivot.yanki.domain.exception.BusinessRuleException;
import com.pe.pivot.yanki.domain.exception.WalletNotFoundException;
import com.pe.pivot.yanki.domain.model.YankiWalletStatus;
import com.pe.pivot.yanki.domain.port.in.DeleteYankiWalletUseCase;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use Case for logically closing (Soft Delete) a Yanki wallet.
 */
@Service
@RequiredArgsConstructor
public class DeleteYankiWalletUseCaseImpl implements DeleteYankiWalletUseCase {

  private final YankiWalletRepositoryPort walletRepository;

  /**
   * Deletes a Yanki wallet by its phone number.
   *
   * @param phoneNumber - the phone number associated with the Yanki wallet to be deleted
   * @return - a Completable that completes when the wallet is successfully deleted
   */
  @Override
  public Completable delete(String phoneNumber) {
    // Validate that the phone number is not null or empty
    return walletRepository.findByPhoneNumber(phoneNumber)
            .switchIfEmpty(Maybe.error(new WalletNotFoundException(
                    "Yanki wallet not found for phone number: " + phoneNumber)))
            .flatMapCompletable(existing -> {

              // A CLOSED wallet cannot be deleted again
              if (existing.getStatus() == YankiWalletStatus.CLOSED) {
                return Completable.error(
                        new BusinessRuleException("Yanki wallet is already CLOSED."));
              }
              existing.setStatus(YankiWalletStatus.CLOSED);
              existing.setUpdatedAt(Instant.now());
              return walletRepository.save(existing).ignoreElement();
            });
  }
}