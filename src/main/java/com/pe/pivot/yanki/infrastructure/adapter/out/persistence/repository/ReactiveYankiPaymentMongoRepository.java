package com.pe.pivot.yanki.infrastructure.adapter.out.persistence.repository;

import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.entity.YankiPaymentDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive Spring Data MongoDB repository for YankiPaymentDocument (zero @Query).
 */
public interface ReactiveYankiPaymentMongoRepository
        extends ReactiveMongoRepository<YankiPaymentDocument, String> {

  /**
   * Finds a YankiPaymentDocument by its transactionId.
   *
   * @param transactionId - the unique transaction identifier to search for
   * @return - a Mono emitting the found YankiPaymentDocument, or empty if not found
   */
  Mono<YankiPaymentDocument> findByTransactionId(String transactionId);

  /**
   * Finds YankiPaymentDocuments by either senderPhoneNumber or receiverPhoneNumber.
   *
   * @param senderPhoneNumber - the phone number of the sender to search for
   * @param receiverPhoneNumber - the phone number of the receiver to search for
   * @return - a Flux emitting all YankiPaymentDocuments matching either phone number
   */
  Flux<YankiPaymentDocument> findBySenderPhoneNumberOrReceiverPhoneNumber(
          String senderPhoneNumber, String receiverPhoneNumber);
}