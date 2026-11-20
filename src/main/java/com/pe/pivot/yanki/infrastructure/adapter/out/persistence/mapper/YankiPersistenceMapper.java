package com.pe.pivot.yanki.infrastructure.adapter.out.persistence.mapper;

import com.pe.pivot.yanki.domain.model.YankiDocumentType;
import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.model.YankiPaymentStatus;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.model.YankiWalletStatus;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.entity.YankiPaymentDocument;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.entity.YankiWalletDocument;
import org.springframework.stereotype.Component;

/**
 * Mapper converting between Yanki domain models and MongoDB persistence documents (SRP).
 */
@Component
public class YankiPersistenceMapper {

  /**
   * Maps a YankiWallet domain object to a YankiWalletDocument for persistence in MongoDB.
   *
   * @param domain - The YankiWallet domain object to be converted.
   * @return - The corresponding YankiWalletDocument for MongoDB storage.
   */
  public YankiWalletDocument toWalletDocument(YankiWallet domain) {
    return YankiWalletDocument.builder()
            .id(domain.getId())
            .documentType(domain.getDocumentType() != null ? domain.getDocumentType().name() : null)
            .documentNumber(domain.getDocumentNumber())
            .phoneNumber(domain.getPhoneNumber())
            .imei(domain.getImei())
            .email(domain.getEmail())
            .balance(domain.getBalance())
            .linkedDebitCardId(domain.getLinkedDebitCardId())
            .status(domain.getStatus() != null ? domain.getStatus().name() : null)
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
  }

  /**
   * Maps a YankiWalletDocument from MongoDB to a YankiWallet domain object.
   *
   * @param doc - The YankiWalletDocument retrieved from MongoDB.
   * @return - The corresponding YankiWallet domain object.
   */
  public YankiWallet toWalletDomain(YankiWalletDocument doc) {
    return YankiWallet.builder()
            .id(doc.getId())
            .documentType(doc.getDocumentType() != null
                    ? YankiDocumentType.valueOf(doc.getDocumentType()) : null)
            .documentNumber(doc.getDocumentNumber())
            .phoneNumber(doc.getPhoneNumber())
            .imei(doc.getImei())
            .email(doc.getEmail())
            .balance(doc.getBalance())
            .linkedDebitCardId(doc.getLinkedDebitCardId())
            .status(doc.getStatus() != null ? YankiWalletStatus.valueOf(doc.getStatus()) : null)
            .createdAt(doc.getCreatedAt())
            .updatedAt(doc.getUpdatedAt())
            .build();
  }

  /**
   * Maps a YankiPayment domain object to a YankiPaymentDocument for persistence in MongoDB.
   *
   * @param domain - The YankiPayment domain object to be converted.
   * @return - The corresponding YankiPaymentDocument for MongoDB storage.
   */
  public YankiPaymentDocument toPaymentDocument(YankiPayment domain) {
    return YankiPaymentDocument.builder()
            .id(domain.getId())
            .transactionId(domain.getTransactionId())
            .senderPhoneNumber(domain.getSenderPhoneNumber())
            .receiverPhoneNumber(domain.getReceiverPhoneNumber())
            .senderDebitCardId(domain.getSenderDebitCardId())
            .receiverDebitCardId(domain.getReceiverDebitCardId())
            .currency(domain.getCurrency())
            .amount(domain.getAmount())
            .description(domain.getDescription())
            .status(domain.getStatus() != null ? domain.getStatus().name() : null)
            .failureReason(domain.getFailureReason())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
  }

  /**
   * Maps a YankiPaymentDocument from MongoDB to a YankiPayment domain object.
   *
   * @param doc - The YankiPaymentDocument retrieved from MongoDB.
   * @return - The corresponding YankiPayment domain object.
   */
  public YankiPayment toPaymentDomain(YankiPaymentDocument doc) {
    return YankiPayment.builder()
            .id(doc.getId())
            .transactionId(doc.getTransactionId())
            .senderPhoneNumber(doc.getSenderPhoneNumber())
            .receiverPhoneNumber(doc.getReceiverPhoneNumber())
            .senderDebitCardId(doc.getSenderDebitCardId())
            .receiverDebitCardId(doc.getReceiverDebitCardId())
            .currency(doc.getCurrency())
            .amount(doc.getAmount())
            .description(doc.getDescription())
            .status(doc.getStatus() != null ? YankiPaymentStatus.valueOf(doc.getStatus()) : null)
            .failureReason(doc.getFailureReason())
            .createdAt(doc.getCreatedAt())
            .updatedAt(doc.getUpdatedAt())
            .build();
  }
}