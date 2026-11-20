package com.pe.pivot.yanki.infrastructure.adapter.out.messaging;

import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.port.out.YankiEventPublisherPort;
import com.pe.pivot.yanki.infrastructure.adapter.in.messaging.dto.YankiPaymentResultEvent;
import com.pe.pivot.yanki.infrastructure.adapter.out.messaging.dto.YankiPaymentRequestedEvent;
import io.reactivex.rxjava3.core.Completable;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka Producer Adapter publishing Yanki payment requests and wallet-to-wallet results.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YankiKafkaProducerAdapter implements YankiEventPublisherPort {

  // KafkaTemplate for publishing events to Kafka topics
  private final KafkaTemplate<String, Object> kafkaTemplate;

  // Topic names for payment requested and payment result events, injected from application properties
  @Value("${application.yanki.topics.payment-requested}")
  private String paymentRequestedTopic;

   // Topic name for payment result events, injected from application properties
  @Value("${application.yanki.topics.payment-result}")
  private String paymentResultTopic;

  /**
   * Publishes a YankiPaymentRequestedEvent to the configured Kafka topic when a payment is requested.
   *
   * @param payment - the YankiPayment object containing the payment details
   * @return - a Completable that completes when the event is successfully published
   */
  @Override
  public Completable publishPaymentRequested(YankiPayment payment) {
    // Create a YankiPaymentRequestedEvent with the payment details and current timestamp
    YankiPaymentRequestedEvent event = YankiPaymentRequestedEvent.builder()
            .transactionId(payment.getTransactionId())
            .senderPhoneNumber(payment.getSenderPhoneNumber())
            .receiverPhoneNumber(payment.getReceiverPhoneNumber())
            .senderDebitCardId(payment.getSenderDebitCardId())
            .receiverDebitCardId(payment.getReceiverDebitCardId())
            .currency(payment.getCurrency())
            .amount(payment.getAmount())
            .description(payment.getDescription())
            .requestedAt(Instant.now())
            .build();

    // Publish the YankiPaymentRequestedEvent to the Kafka topic and log the operation
    return Completable.fromCompletionStage(
                    kafkaTemplate.send(paymentRequestedTopic, payment.getTransactionId(), event))
            .doOnComplete(() -> log.info("Published YankiPaymentRequestedEvent for TxId: {}",
                    payment.getTransactionId()));
  }

  /**
   * Publishes a YankiPaymentResultEvent to the configured Kafka topic after processing a payment.
   *
   * @param payment - the YankiPayment object containing the payment details and status
   * @return - a Completable that completes when the event is successfully published
   */
  @Override
  public Completable publishPaymentResult(YankiPayment payment) {
    // Create a YankiPaymentResultEvent with the payment details and current timestamp
    YankiPaymentResultEvent event = YankiPaymentResultEvent.builder()
            .transactionId(payment.getTransactionId())
            .status(payment.getStatus().name())
            .senderPhoneNumber(payment.getSenderPhoneNumber())
            .receiverPhoneNumber(payment.getReceiverPhoneNumber())
            .senderDebitCardId(payment.getSenderDebitCardId())
            .receiverDebitCardId(payment.getReceiverDebitCardId())
            .currency(payment.getCurrency())
            .amount(payment.getAmount())
            .failureReason(payment.getFailureReason())
            .processedAt(Instant.now())
            .build();

    // Publish the YankiPaymentResultEvent to the Kafka topic and log the operation
    return Completable.fromCompletionStage(
                    kafkaTemplate.send(paymentResultTopic, payment.getTransactionId(), event))
            .doOnComplete(() -> log.info("Published YankiPaymentResultEvent for TxId: {}",
                    payment.getTransactionId()));
  }
}