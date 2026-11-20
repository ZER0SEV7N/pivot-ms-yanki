package com.pe.pivot.yanki.infrastructure.adapter.in.messaging;

import com.pe.pivot.yanki.domain.model.YankiPaymentResultCommand;
import com.pe.pivot.yanki.domain.port.in.HandleYankiPaymentResultUseCase;
import com.pe.pivot.yanki.infrastructure.adapter.in.messaging.dto.YankiPaymentResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer Adapter listening to yanki.payment.result to finalize or compensate payments.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YankiKafkaConsumerAdapter {

  private final HandleYankiPaymentResultUseCase handleResultUseCase;

  @KafkaListener(
          topics = "${application.yanki.topics.payment-result}",
          groupId = "${spring.kafka.consumer.group-id}")
  public void onPaymentResult(YankiPaymentResultEvent event) {
    log.info("Received YankiPaymentResultEvent for TxId: {}, Status: {}",
            event.transactionId(), event.status());

    YankiPaymentResultCommand command = YankiPaymentResultCommand.builder()
            .transactionId(event.transactionId())
            .status(event.status())
            .failureReason(event.failureReason())
            .processedAt(event.processedAt())
            .build();

    handleResultUseCase.handleResult(command)
            .doOnError(err -> log.error("Error handling Yanki payment result {}: {}",
                    event.transactionId(), err.getMessage(), err))
            .onErrorComplete()
            .blockingAwait();
  }
}