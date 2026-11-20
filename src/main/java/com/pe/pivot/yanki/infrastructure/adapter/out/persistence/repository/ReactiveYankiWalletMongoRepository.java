package com.pe.pivot.yanki.infrastructure.adapter.out.persistence.repository;

import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.entity.YankiWalletDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Reactive Spring Data MongoDB repository for YankiWalletDocument (zero @Query).
 */
public interface ReactiveYankiWalletMongoRepository
        extends ReactiveMongoRepository<YankiWalletDocument, String> {

  /**
   * Finds a YankiWalletDocument by its phone number.
   *
   * @param phoneNumber - the phone number to search for
   * @return - a Mono emitting the found YankiWalletDocument, or empty if not found
   */
  Mono<YankiWalletDocument> findByPhoneNumber(String phoneNumber);

  /**
   * Checks if a YankiWalletDocument exists with the specified phone number.
   *
   * @param phoneNumber - the phone number to check for existence
   * @return - a Mono emitting true if a document exists, false otherwise
   */
  Mono<Boolean> existsByPhoneNumber(String phoneNumber);

  /**
   * Checks if a YankiWalletDocument exists with the specified IMEI.
   *
   * @param imei - the IMEI to check for existence
   * @return - a Mono emitting true if a document exists, false otherwise
   */
  Mono<Boolean> existsByImei(String imei);

  /**
   * Checks if a YankiWalletDocument exists with the specified document number.
   *
   * @param documentNumber - the document number to check for existence
   * @return - a Mono emitting true if a document exists, false otherwise
   */
  Mono<Boolean> existsByDocumentNumber(String documentNumber);
}