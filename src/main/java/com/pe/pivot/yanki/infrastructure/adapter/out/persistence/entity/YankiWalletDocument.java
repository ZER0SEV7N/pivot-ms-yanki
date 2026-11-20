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
 * MongoDB document representing a Yanki mobile wallet in db_pivot_yanki.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "yanki_wallets")
public class YankiWalletDocument {

  @Id
  private String id;
  private String documentType;

  @Indexed(unique = true)
  private String documentNumber;

  @Indexed(unique = true)
  private String phoneNumber;

  @Indexed(unique = true)
  private String imei;

  private String email;
  private BigDecimal balance;
  private String linkedDebitCardId;
  private String status;
  private Instant createdAt;
  private Instant updatedAt;
}