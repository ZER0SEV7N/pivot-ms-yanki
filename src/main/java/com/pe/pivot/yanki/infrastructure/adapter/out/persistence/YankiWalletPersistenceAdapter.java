package com.pe.pivot.yanki.infrastructure.adapter.out.persistence;

import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.mapper.YankiPersistenceMapper;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.repository.ReactiveYankiWalletMongoRepository;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava3Adapter;

/**
 * MongoDB Persistence Adapter implementing YankiWalletRepositoryPort with RxJava 3.
 */
@Component
@RequiredArgsConstructor
public class YankiWalletPersistenceAdapter implements YankiWalletRepositoryPort {

  private final ReactiveYankiWalletMongoRepository repository;
  private final YankiPersistenceMapper mapper;

  /**
   * Saves a YankiWallet to the MongoDB database.
   *
   * @param wallet - the YankiWallet domain object to be saved
   * @return - a Single emitting the saved YankiWallet domain object
   */
  @Override
  public Single<YankiWallet> save(YankiWallet wallet) {
    return RxJava3Adapter.monoToSingle(
            repository.save(mapper.toWalletDocument(wallet)).map(mapper::toWalletDomain));
  }

  /**
   * Retrieves all YankiWallet records from the MongoDB database.
   *
   * @return - a Flowable emitting all YankiWallet domain objects
   */
  @Override
  public Flowable<YankiWallet> findAll() {
    return RxJava3Adapter.fluxToFlowable(repository.findAll().map(mapper::toWalletDomain));
  }

  /**
   * Finds a YankiWallet by its unique identifier (phone number).
   *
   * @param phoneNumber - the unique identifier (phone number) of the YankiWallet
   * @return - a Maybe emitting the found YankiWallet or empty if not found
   */
  @Override
  public Maybe<YankiWallet> findByPhoneNumber(String phoneNumber) {
    return RxJava3Adapter.monoToMaybe(
            repository.findByPhoneNumber(phoneNumber).map(mapper::toWalletDomain));
  }

  /**
   * Checks if a YankiWallet exists with the specified phone number.
   *
   * @param phoneNumber - the phone number to check for existence
   * @return - a Single emitting true if a wallet exists, false otherwise
   */
  @Override
  public Single<Boolean> existsByPhoneNumber(String phoneNumber) {
    return RxJava3Adapter.monoToSingle(repository.existsByPhoneNumber(phoneNumber));
  }

  /**
   * Checks if a YankiWallet exists with the specified IMEI.
   *
   * @param imei - the IMEI to check for existence
   * @return - a Single emitting true if a wallet exists, false otherwise
   */
  @Override
  public Single<Boolean> existsByImei(String imei) {
    return RxJava3Adapter.monoToSingle(repository.existsByImei(imei));
  }

  /**
   * Checks if a YankiWallet exists with the specified document number.
   *
   * @param documentNumber - the document number to check for existence
   * @return - a Single emitting true if a wallet exists, false otherwise
   */
  @Override
  public Single<Boolean> existsByDocumentNumber(String documentNumber) {
    return RxJava3Adapter.monoToSingle(repository.existsByDocumentNumber(documentNumber));
  }
}