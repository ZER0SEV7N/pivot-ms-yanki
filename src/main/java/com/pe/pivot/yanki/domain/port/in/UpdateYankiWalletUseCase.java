package com.pe.pivot.yanki.domain.port.in;

import com.pe.pivot.yanki.domain.model.YankiWallet;
import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;

/**
 * Use case interface for updating Yanki wallet information,
 * linking debit cards, and topping up the wallet balance.
 * This interface defines the contract for operations that can be performed on a Yanki wallet, including
 * updating wallet details, linking a debit card, and adding funds to the wallet.
 */
public interface UpdateYankiWalletUseCase {

  /**
   * Updates the Yanki wallet information for a
   * given phone number with the provided incoming wallet data.
   *
   * @param phoneNumber - the phone number associated with the Yanki wallet to be updated
   * @param incoming - the incoming YankiWallet object containing updated information
   * @return - a Single emitting the updated YankiWallet after the update operation
   */
  Single<YankiWallet> update(String phoneNumber, YankiWallet incoming);

  /**
   * Links a debit card to the Yanki wallet associated with the given phone number.
   *
   * @param phoneNumber - the phone number associated with the Yanki wallet to which the debit card will be linked
   * @param debitCardId - the identifier of the debit card to be linked to the Yanki wallet
   * @return - a Single emitting the updated YankiWallet after linking the debit card
   */
  Single<YankiWallet> linkDebitCard(String phoneNumber, String debitCardId);

  /**
   * Adds funds to the Yanki wallet associated with the given phone number by topping up the specified amount.
   *
   * @param phoneNumber - the phone number associated with the Yanki wallet to be topped up
   * @param amount - the amount to be added to the Yanki wallet balance
   * @return - a Single emitting the updated YankiWallet after the top-up operation
   */
  Single<YankiWallet> topUp(String phoneNumber, BigDecimal amount);
}
