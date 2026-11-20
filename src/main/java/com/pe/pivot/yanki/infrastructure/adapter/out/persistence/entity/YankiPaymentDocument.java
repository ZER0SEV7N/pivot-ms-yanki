package com.pe.pivot.yanki.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a Yanki phone-to-phone payment in db_pivot_yanki.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "yanki_payments")
public class YankiPaymentDocument {

  @Id
  private String id;

  @Indexed(unique = true)
  private String transactionId;

  @Indexed
  private String senderPhoneNumber;

  @Indexed
  private String receiverPhoneNumber;

  private String senderDebitCardId;
  private String receiverDebitCardId;
  private String currency;
  private BigDecimal amount;
  private String description;
  private String status;
  private String failureReason;
  private Instant createdAt;
  private Instant updatedAt;
}