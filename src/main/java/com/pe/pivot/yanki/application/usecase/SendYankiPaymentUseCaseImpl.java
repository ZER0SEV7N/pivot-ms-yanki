package com.pe.pivot.yanki.application.usecase;

import com.pe.pivot.yanki.application.support.YankiPaymentFactory;
import com.pe.pivot.yanki.domain.exception.BusinessRuleException;
import com.pe.pivot.yanki.domain.exception.WalletNotFoundException;
import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.model.YankiPaymentStatus;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.port.in.SendYankiPaymentUseCase;
import com.pe.pivot.yanki.domain.port.out.YankiEventPublisherPort;
import com.pe.pivot.yanki.domain.port.out.YankiPaymentRepositoryPort;
import com.pe.pivot.yanki.domain.port.out.YankiWalletRepositoryPort;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case orchestrating phone-to-phone Yanki payments (instant wallet-to-wallet or
 * asynchronous Kafka Saga when a DebitCard is linked).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SendYankiPaymentUseCaseImpl implements SendYankiPaymentUseCase {

  // Dependencies injected via constructor
  private final YankiWalletRepositoryPort walletRepository;
  private final YankiPaymentRepositoryPort paymentRepository;
  private final YankiEventPublisherPort eventPublisher;
  private final YankiPaymentFactory paymentFactory;

  /**
   * Sends a Yanki payment from one wallet to another, handling both instant
   * transfers and asynchronous bank-card settlements.
   *
   * @param incoming - the YankiPayment object containing payment details to be sent
   * @return - a Single emitting the resulting YankiPayment after processing
   */
  @Override
  public Single<YankiPayment> sendPayment(YankiPayment incoming) {
    validateRequest(incoming);

    // Find sender and receiver wallets concurrently, then execute the transfer
    return Single.zip(
                    findActiveWallet(incoming.getSenderPhoneNumber(), "Sender"),
                    findActiveWallet(incoming.getReceiverPhoneNumber(), "Receiver"),
                    (sender, receiver) -> executeTransfer(incoming, sender, receiver))
            .flatMap(single -> single);
  }

  /**
   * Validates the incoming YankiPayment request to ensure that the amount is positive and that
   * the sender and receiver phone numbers are different.
   *
   * @param incoming - the YankiPayment object containing payment details to be validated
   * @throws BusinessRuleException if the payment amount is not greater than zero or if the
   */
  private void validateRequest(YankiPayment incoming) {
    // Validate that the payment amount is greater than zero
    if (incoming.getAmount() == null || incoming.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessRuleException("Payment amount must be greater than zero.");
    }

    // Validate that the sender and receiver phone numbers are not the same
    if (incoming.getSenderPhoneNumber().equals(incoming.getReceiverPhoneNumber())) {
      throw new BusinessRuleException("Sender and receiver phone numbers must be different.");
    }
  }

  /**
   * Finds an active Yanki wallet by phone number and validates its status.
   *
   * @param phone - the phone number associated with the Yanki wallet to be found
   * @param roleLabel - a label indicating whether the wallet is for the sender or receiver,
   *                    used for error messages
   * @return - a Single emitting the found active YankiWallet, or an error if not
   */
  private Single<YankiWallet> findActiveWallet(String phone, String roleLabel) {
    return walletRepository.findByPhoneNumber(phone)
            // If the wallet is not found, emit a WalletNotFoundException with a descriptive message
            .switchIfEmpty(Single.error(new WalletNotFoundException(
                    roleLabel + " Yanki wallet not found for phone: " + phone)))
            // Validate that the found wallet is active; if not, emit a BusinessRuleException
            .flatMap(wallet -> wallet.isActive()
                    ? Single.just(wallet)
                    : Single.error(new BusinessRuleException(
                    roleLabel + " Yanki wallet is not ACTIVE.")));
  }

  /**
   * Executes the transfer of funds between the sender and receiver wallets.
   *
   * @param incoming - the YankiPayment object containing payment details to be processed
   * @param sender - the YankiWallet object representing the sender's wallet
   * @param receiver - the YankiWallet object representing the receiver's wallet
   * @return - a Single emitting the resulting YankiPayment after processing
   */
  private Single<YankiPayment> executeTransfer(
          YankiPayment incoming, YankiWallet sender, YankiWallet receiver) {
    boolean usesBankCard = sender.hasLinkedCard() || receiver.hasLinkedCard();

    // If neither wallet has a linked bank card,
    // perform an instant transfer by debiting the sender and crediting the receiver
    if (!usesBankCard) {
      sender.debit(incoming.getAmount());
      receiver.credit(incoming.getAmount());
      YankiPayment payment = paymentFactory.create(
              incoming, sender, receiver, YankiPaymentStatus.COMPLETED);

      return walletRepository.save(sender)
              .flatMap(s -> walletRepository.save(receiver))
              .flatMap(r -> paymentRepository.save(payment))
              .flatMap(saved -> eventPublisher.publishPaymentResult(saved)
                      .andThen(Single.just(saved)));
    }

    // If either wallet has a linked bank card,
    // create a pending payment and publish a payment requested event for asynchronous processing
    // The sender wallet is debited; the receiver is credited asynchronously
    Single<YankiWallet> senderReady = sender.hasLinkedCard()
            ? Single.just(sender)
            : Single.defer(() -> {
              sender.debit(incoming.getAmount());
              return walletRepository.save(sender);
            });

    // Create a PENDING payment to be processed asynchronously
    YankiPayment pendingPayment = paymentFactory.create(
            incoming, sender, receiver, YankiPaymentStatus.PENDING);

    // Publish a payment requested event for asynchronous processing when a bank card is involved
    return senderReady
            .flatMap(ignored -> paymentRepository.save(pendingPayment))
            .flatMap(saved -> eventPublisher.publishPaymentRequested(saved)
                    .andThen(Single.just(saved)));
  }
}