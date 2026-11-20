package com.pe.pivot.yanki.application.usecase;

import com.pe.pivot.yanki.domain.exception.BusinessRuleException;
import com.pe.pivot.yanki.domain.exception.WalletNotFoundException;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.model.YankiWalletStatus;
import com.pe.pivot.yanki.domain.port.in.UpdateYankiWalletUseCase;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case for updating Yanki wallet metadata, linking a DebitCard, or topping up balance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateYankiWalletUseCaseImpl implements UpdateYankiWalletUseCase {

  private final YankiWalletRepositoryPort walletRepository;

  /**
   * Updates the metadata of an existing Yanki wallet identified by the provided phone number.
   *
   * @param phoneNumber - the phone number associated with the Yanki wallet to be updated
   * @param incoming - the incoming YankiWallet object containing updated information
   * @return - a Single emitting the updated YankiWallet object
   */
  @Override
  public Single<YankiWallet> update(String phoneNumber, YankiWallet incoming) {
    return findModifiableWallet(phoneNumber)
            .flatMap(existing -> {
              Optional.ofNullable(incoming.getEmail())
                      .map(e -> e.trim().toLowerCase())
                      .ifPresent(existing::setEmail);
              Optional.ofNullable(incoming.getImei())
                      .map(String::trim)
                      .ifPresent(existing::setImei);
              Optional.ofNullable(incoming.getStatus()).ifPresent(existing::setStatus);
              existing.setUpdatedAt(Instant.now());
              return walletRepository.save(existing);
            });
  }

  /**
   * Links a DebitCard to an existing Yanki wallet identified by the provided phone number.
   *
   * @param phoneNumber - the phone number associated with the Yanki wallet to which the debit card will be linked
   * @param debitCardId - the identifier of the debit card to be linked to the Yanki wallet
   * @return - a Single emitting the updated YankiWallet object with the linked debit card
   */
  @Override
  public Single<YankiWallet> linkDebitCard(String phoneNumber, String debitCardId) {
    // Validate that the debitCardId is not null or empty
    return findModifiableWallet(phoneNumber)
            .flatMap(existing -> {
              existing.setLinkedDebitCardId(debitCardId.trim());
              existing.setUpdatedAt(Instant.now());
              return walletRepository.save(existing);
            })
            .doOnSuccess(w -> log.info("Linked debitCard {} to Yanki phone {}",
                    debitCardId, phoneNumber));
  }

  /**
   * Tops up the balance of an existing Yanki wallet identified by the provided phone number.
   *
   * @param phoneNumber - the phone number associated with the Yanki wallet to be topped up
   * @param amount - the amount to be added to the Yanki wallet balance
   * @return - a Single emitting the updated YankiWallet object with the new balance
   */
  @Override
  public Single<YankiWallet> topUp(String phoneNumber, BigDecimal amount) {
    // Validate that the top-up amount is greater than zero
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      return Single.error(new BusinessRuleException("Top-up amount must be greater than zero."));
    }
    return findModifiableWallet(phoneNumber)
            .flatMap(existing -> {
              existing.credit(amount);
              return walletRepository.save(existing);
            });
  }

  /**
   * Finds a Yanki wallet by phone number and ensures it is modifiable (not CLOSED).
   *
   * @param phoneNumber - the phone number associated with the Yanki wallet to be retrieved
   * @return - a Single emitting the found YankiWallet object if it exists and is modifiable, or an error if not
   */
  private Single<YankiWallet> findModifiableWallet(String phoneNumber) {
    return walletRepository.findByPhoneNumber(phoneNumber)
            .switchIfEmpty(Single.error(new WalletNotFoundException(
                    "Yanki wallet not found for phone number: " + phoneNumber)))
            .flatMap(wallet -> wallet.getStatus() == YankiWalletStatus.CLOSED
                    ? Single.error(new BusinessRuleException("Cannot modify a CLOSED Yanki wallet."))
                    : Single.just(wallet));
  }
}
