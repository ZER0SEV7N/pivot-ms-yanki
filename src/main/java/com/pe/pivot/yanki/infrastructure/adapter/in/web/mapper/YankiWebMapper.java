package com.pe.pivot.yanki.infrastructure.adapter.in.web.mapper;

import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiPaymentRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiPaymentResponse;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiWalletRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiWalletResponse;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiWalletUpdateRequest;
import org.springframework.stereotype.Component;

/**
 * Web Mapper translating between REST DTOs and Yanki domain models (SRP).
 */
@Component
public class YankiWebMapper {

  /**
   * Maps a YankiWalletRequest DTO to a YankiWallet domain object for creation.
   *
   * @param request - the YankiWalletRequest DTO containing wallet creation details
   * @return - the corresponding YankiWallet domain object
   */
  public YankiWallet toDomain(YankiWalletRequest request) {
    return YankiWallet.builder()
            .documentType(request.documentType())
            .documentNumber(request.documentNumber())
            .phoneNumber(request.phoneNumber())
            .imei(request.imei())
            .email(request.email())
            .balance(request.initialBalance())
            .build();
  }

  /**
   * Maps a YankiWalletUpdateRequest DTO to a YankiWallet domain object for updating wallet details.
   *
   * @param request - the YankiWalletUpdateRequest DTO containing updated wallet information
   * @return - the corresponding YankiWallet domain object with updated fields
   */
  public YankiWallet toDomain(YankiWalletUpdateRequest request) {
    return YankiWallet.builder()
            .email(request.email())
            .imei(request.imei())
            .status(request.status())
            .build();
  }

  /**
   * Maps a YankiWallet domain object to a YankiWalletResponse DTO for API response.
   *
   * @param wallet - the YankiWallet domain object to be converted
   * @return - the corresponding YankiWalletResponse DTO
   */
  public YankiWalletResponse toWalletResponse(YankiWallet wallet) {
    return YankiWalletResponse.builder()
            .id(wallet.getId())
            .documentType(wallet.getDocumentType())
            .documentNumber(wallet.getDocumentNumber())
            .phoneNumber(wallet.getPhoneNumber())
            .imei(wallet.getImei())
            .email(wallet.getEmail())
            .balance(wallet.getBalance())
            .linkedDebitCardId(wallet.getLinkedDebitCardId())
            .status(wallet.getStatus())
            .createdAt(wallet.getCreatedAt())
            .updatedAt(wallet.getUpdatedAt())
            .build();
  }

  /**
   * Maps a YankiPaymentRequest DTO to a YankiPayment domain object for processing.
   *
   * @param request - the YankiPaymentRequest DTO containing payment details
   * @return - the corresponding YankiPayment domain object
   */
  public YankiPayment toDomain(YankiPaymentRequest request) {
    return YankiPayment.builder()
            .senderPhoneNumber(request.senderPhoneNumber())
            .receiverPhoneNumber(request.receiverPhoneNumber())
            .currency(request.currency())
            .amount(request.amount())
            .description(request.description())
            .build();
  }

  /**
   * Maps a YankiPayment domain object to a YankiPaymentResponse DTO for API response.
   *
   * @param payment - the YankiPayment domain object to be converted
   * @return - the corresponding YankiPaymentResponse DTO
   */
  public YankiPaymentResponse toPaymentResponse(YankiPayment payment) {
    return YankiPaymentResponse.builder()
            .id(payment.getId())
            .transactionId(payment.getTransactionId())
            .senderPhoneNumber(payment.getSenderPhoneNumber())
            .receiverPhoneNumber(payment.getReceiverPhoneNumber())
            .currency(payment.getCurrency())
            .amount(payment.getAmount())
            .description(payment.getDescription())
            .status(payment.getStatus())
            .failureReason(payment.getFailureReason())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .build();
  }
}