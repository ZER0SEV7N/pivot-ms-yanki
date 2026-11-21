package com.pe.pivot.yanki.application.usecase;

import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.model.YankiPaymentResultCommand;
import com.pe.pivot.yanki.domain.model.YankiPaymentStatus;
import com.pe.pivot.yanki.domain.port.in.HandleYankiPaymentResultUseCase;
import com.pe.pivot.yanki.domain.port.out.YankiPaymentRepositoryPort;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import io.reactivex.rxjava3.core.Completable;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case handling asynchronous Kafka settlement outcomes from ms-account,
 * including compensating refunds to unlinked sender wallets on FAILED outcomes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HandleYankiPaymentResultUseCaseImpl implements HandleYankiPaymentResultUseCase {

  private final YankiPaymentRepositoryPort paymentRepository;
  private final YankiWalletRepositoryPort walletRepository;

  /**
   * Handles the result of a Yanki payment transaction, updating the payment status
   * and performing necessary compensations.
   *
   * @param command - the command containing the details of the payment result to handle
   * @return - a Completable that completes when the payment result has been handled
   */
  @Override
  public Completable handleResult(YankiPaymentResultCommand command) {
    return paymentRepository.findByTransactionId(command.transactionId())
            .flatMapCompletable(payment -> {
              if (payment.getStatus() != YankiPaymentStatus.PENDING) {
                log.debug("Idempotency guard: Yanki payment {} already finalized as {}.",
                        payment.getTransactionId(), payment.getStatus());
                return Completable.complete();
              }
              return command.isCompleted()
                      ? finalizeCompleted(payment)
                      : finalizeFailedWithCompensation(payment, command.failureReason());
            });
  }

  /**
   * Finalizes a completed Yanki payment by updating its status
   * to COMPLETED and crediting the receiver's wallet if necessary.
   *
   * @param payment - the YankiPayment object representing the payment to be finalized
   * @return - a Completable that completes when the payment has been finalized
   */
  private Completable finalizeCompleted(YankiPayment payment) {
    boolean receiverNeedsWalletCredit = payment.getReceiverDebitCardId() == null
            || payment.getReceiverDebitCardId().isBlank();

    // If the receiver does not have a linked debit card,
    // credit their Yanki wallet with the payment amount
    Completable creditStep = receiverNeedsWalletCredit
            ? walletRepository.findByPhoneNumber(payment.getReceiverPhoneNumber())
            .flatMapCompletable(wallet -> {
              wallet.credit(payment.getAmount());
              return walletRepository.save(wallet).ignoreElement();
            })
            : Completable.complete();

    payment.setStatus(YankiPaymentStatus.COMPLETED);
    payment.setUpdatedAt(Instant.now());
    return creditStep.andThen(paymentRepository.save(payment).ignoreElement());
  }

  /**
   * Finalizes a failed Yanki payment by updating its status to FAILED.
   *
   * @param payment - the YankiPayment object representing the payment to be finalized
   * @param reason - the reason for the payment failure, used for logging
   * @return - a Completable that completes when the payment has been finalized and compensated
   */
  private Completable finalizeFailedWithCompensation(YankiPayment payment, String reason) {
    boolean senderNeedsWalletRefund = payment.getSenderDebitCardId() == null
            || payment.getSenderDebitCardId().isBlank();

    // If the sender does not have a linked debit card,
    // refund their Yanki wallet with the payment amount
    final Completable refundStep = senderNeedsWalletRefund
            ? walletRepository.findByPhoneNumber(payment.getSenderPhoneNumber())
            .flatMapCompletable(wallet -> {
              log.warn("Compensating sender Yanki wallet {} for failed TxId {}",
                      wallet.getPhoneNumber(), payment.getTransactionId());
              wallet.credit(payment.getAmount());
              return walletRepository.save(wallet).ignoreElement();
            })
            : Completable.complete();

    // Update the payment status to FAILED and record the failure reason
    payment.setStatus(YankiPaymentStatus.FAILED);
    payment.setFailureReason(reason);
    payment.setUpdatedAt(Instant.now());
    return refundStep.andThen(paymentRepository.save(payment).ignoreElement());
  }
}