package com.pe.pivot.yanki.infrastructure.adapter.in.web.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.pe.pivot.yanki.domain.model.YankiDocumentType;
import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.model.YankiPaymentStatus;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.model.YankiWalletStatus;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiPaymentRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiPaymentResponse;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiWalletRequest;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiWalletResponse;
import com.pe.pivot.yanki.infrastructure.adapter.in.web.dto.YankiWalletUpdateRequest;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for YankiWebMapper.
 */
class YankiWebMapperTest {

  private YankiWebMapper mapper;
  private Instant now;

  @BeforeEach
  void setUp() {
    mapper = new YankiWebMapper();
    now = Instant.parse("2026-01-01T00:00:00Z");
  }

  @Test
  @DisplayName("Should be map a wallet creation request to the domain (happy path)")
  void shouldBeMapWalletRequestToDomain() {
    // Arrange
    YankiWalletRequest request = YankiWalletRequest.builder()
            .documentType(YankiDocumentType.DNI)
            .documentNumber("12345678")
            .phoneNumber("999111222")
            .imei("IMEI-1")
            .email("john@mail.com")
            .initialBalance(new BigDecimal("50.00"))
            .build();

    // Act
    YankiWallet wallet = mapper.toDomain(request);

    // Assert
    assertEquals(YankiDocumentType.DNI, wallet.getDocumentType());
    assertEquals("12345678", wallet.getDocumentNumber());
    assertEquals("999111222", wallet.getPhoneNumber());
    assertEquals("IMEI-1", wallet.getImei());
    assertEquals("john@mail.com", wallet.getEmail());
    assertEquals(new BigDecimal("50.00"), wallet.getBalance());
  }

  @Test
  @DisplayName("Should be map a wallet update request to the domain")
  void shouldBeMapWalletUpdateRequestToDomain() {
    // Arrange
    YankiWalletUpdateRequest request = YankiWalletUpdateRequest.builder()
            .email("jane@mail.com")
            .imei("IMEI-2")
            .status(YankiWalletStatus.BLOCKED)
            .build();

    // Act
    YankiWallet wallet = mapper.toDomain(request);

    // Assert
    assertEquals("jane@mail.com", wallet.getEmail());
    assertEquals("IMEI-2", wallet.getImei());
    assertEquals(YankiWalletStatus.BLOCKED, wallet.getStatus());
  }

  @Test
  @DisplayName("Should be map a payment request to the domain")
  void shouldBeMapPaymentRequestToDomain() {
    // Arrange
    YankiPaymentRequest request = YankiPaymentRequest.builder()
            .senderPhoneNumber("999111222")
            .receiverPhoneNumber("999333444")
            .currency("USD")
            .amount(new BigDecimal("20.00"))
            .description("Lunch")
            .build();

    // Act
    YankiPayment payment = mapper.toDomain(request);

    // Assert
    assertEquals("999111222", payment.getSenderPhoneNumber());
    assertEquals("999333444", payment.getReceiverPhoneNumber());
    assertEquals("USD", payment.getCurrency());
    assertEquals(new BigDecimal("20.00"), payment.getAmount());
    assertEquals("Lunch", payment.getDescription());
  }

  @Test
  @DisplayName("Should be map a wallet domain object to its response DTO")
  void shouldBeMapWalletToResponse() {
    // Arrange
    YankiWallet wallet = YankiWallet.builder()
            .id("wallet-1")
            .documentType(YankiDocumentType.CEX)
            .documentNumber("AB1234")
            .phoneNumber("999111222")
            .imei("IMEI-1")
            .email("john@mail.com")
            .balance(new BigDecimal("10.00"))
            .linkedDebitCardId("card-1")
            .status(YankiWalletStatus.ACTIVE)
            .createdAt(now)
            .updatedAt(now)
            .build();

    // Act
    YankiWalletResponse response = mapper.toWalletResponse(wallet);

    // Assert
    assertEquals("wallet-1", response.id());
    assertEquals(YankiDocumentType.CEX, response.documentType());
    assertEquals("AB1234", response.documentNumber());
    assertEquals("999111222", response.phoneNumber());
    assertEquals("IMEI-1", response.imei());
    assertEquals("john@mail.com", response.email());
    assertEquals(new BigDecimal("10.00"), response.balance());
    assertEquals("card-1", response.linkedDebitCardId());
    assertEquals(YankiWalletStatus.ACTIVE, response.status());
    assertEquals(now, response.createdAt());
    assertEquals(now, response.updatedAt());
  }

  @Test
  @DisplayName("Should be map a payment domain object to its response DTO")
  void shouldBeMapPaymentToResponse() {
    // Arrange
    YankiPayment payment = YankiPayment.builder()
            .id("payment-1")
            .transactionId("TX-1")
            .senderPhoneNumber("999111222")
            .receiverPhoneNumber("999333444")
            .currency("PEN")
            .amount(new BigDecimal("30.00"))
            .description("Dinner")
            .status(YankiPaymentStatus.COMPLETED)
            .failureReason(null)
            .createdAt(now)
            .updatedAt(now)
            .build();

    // Act
    YankiPaymentResponse response = mapper.toPaymentResponse(payment);

    // Assert
    assertEquals("payment-1", response.id());
    assertEquals("TX-1", response.transactionId());
    assertEquals("999111222", response.senderPhoneNumber());
    assertEquals("999333444", response.receiverPhoneNumber());
    assertEquals("PEN", response.currency());
    assertEquals(new BigDecimal("30.00"), response.amount());
    assertEquals("Dinner", response.description());
    assertEquals(YankiPaymentStatus.COMPLETED, response.status());
    assertNull(response.failureReason());
    assertEquals(now, response.createdAt());
    assertEquals(now, response.updatedAt());
  }
}
