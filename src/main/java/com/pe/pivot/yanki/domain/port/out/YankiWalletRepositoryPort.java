package com.pe.pivot.yanki.domain.port.out;

import com.pe.pivot.yanki.domain.model.YankiWallet;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Output port for persisting and querying YankiWallet records.
 */
public interface YankiWalletRepositoryPort {

  /**
   * Saves a YankiWallet to the repository.
   *
   * @param wallet - the YankiWallet domain object to be saved
   * @return - a Single emitting the saved YankiWallet domain object
   */
  Single<YankiWallet> save(YankiWallet wallet);

  /**
   * Retrieves all YankiWallet records from the repository.
   *
   * @return - a Flowable emitting all YankiWallet domain objects
   */
  Flowable<YankiWallet> findAll();

  /**
   * Finds a YankiWallet by its unique identifier.
   *
   * @param phoneNumber - the unique identifier (phone number) of the YankiWallet
   * @return - a Maybe emitting the found YankiWallet or empty if not found
   */
  Maybe<YankiWallet> findByPhoneNumber(String phoneNumber);

  /**
   * Checks if a YankiWallet exists with the specified phone number.
   *
   * @param phoneNumber - the phone number to check for existence
   * @return - a Single emitting true if a wallet exists, false otherwise
   */
  Single<Boolean> existsByPhoneNumber(String phoneNumber);

  /**
   * Checks if a YankiWallet exists with the specified IMEI.
   *
   * @param imei - the IMEI to check for existence
   * @return - a Single emitting true if a wallet exists, false otherwise
   */
  Single<Boolean> existsByImei(String imei);

  /**
   * Checks if a YankiWallet exists with the specified document number.
   *
   * @param documentNumber - the document number to check for existence
   * @return - a Single emitting true if a wallet exists, false otherwise
   */
  Single<Boolean> existsByDocumentNumber(String documentNumber);
}