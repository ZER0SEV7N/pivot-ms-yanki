package com.pe.pivot.yanki.application.usecase;

import com.pe.pivot.yanki.domain.exception.PaymentNotFoundException;
import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.port.in.RetrieveYankiPaymentUseCase;
import com.pe.pivot.yanki.domain.port.out.YankiPaymentRepositoryPort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use Case for querying Yanki payment status and phone-number history.
 */
@Service
@RequiredArgsConstructor
public class RetrieveYankiPaymentUseCaseImpl implements RetrieveYankiPaymentUseCase {

  private final YankiPaymentRepositoryPort paymentRepository;

  /**
   * Retrieves a YankiPayment by its associated transaction ID.
   *
   * @param transactionId - the unique identifier of the transaction associated with the payment
   * @return - a Single emitting the YankiPayment if found, or an error if not found
   */
  @Override
  public Single<YankiPayment> findByTransactionId(String transactionId) {
    return paymentRepository.findByTransactionId(transactionId)
            .switchIfEmpty(Single.error(new PaymentNotFoundException(
                    "Yanki payment not found with transactionId: " + transactionId)));
  }

  /**
   * Retrieves all YankiPayments associated with a specific phone number.
   *
   * @param phoneNumber - the phone number associated with the Yanki payments
   * @return - a Flowable emitting all YankiPayments associated with the given phone number
   */
  @Override
  public Flowable<YankiPayment> findByPhoneNumber(String phoneNumber) {
    return paymentRepository.findByPhoneNumber(phoneNumber);
  }
}