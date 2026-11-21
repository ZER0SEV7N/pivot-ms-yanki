package com.pe.pivot.yanki.infrastructure.adapter.out.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.pe.pivot.yanki.domain.model.YankiDocumentType;
import com.pe.pivot.yanki.domain.model.YankiPayment;
import com.pe.pivot.yanki.domain.model.YankiPaymentStatus;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.model.YankiWalletStatus;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.entity.YankiPaymentDocument;
import com.pe.pivot.yanki.infrastructure.adapter.out.persistence.entity.YankiWalletDocument;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit test suite for YankiPersistenceMapper.
 */
class YankiPersistenceMapperTest {

  private YankiPersistenceMapper mapper;
  private Instant now;

  @BeforeEach
  void setUp() {
    mapper = new YankiPersistenceMapper();
    now = Instant.parse("2026-01-01T00:00:00Z");
  }

  @Test
  @DisplayName("Should be map a wallet domain object to a document (happy path)")
  void shouldBeMapWalletDomainToDocument() {
    // Arrange
    YankiWallet domain = YankiWallet.builder()
            .id("wallet-1")
            .documentType(YankiDocumentType.DNI)
            .documentNumber("12345678")
            .phoneNumber("999111222")
            .imei("IMEI-1")
            .email("john@mail.com")
            .balance(new BigDecimal("100.00"))
            .linkedDebitCardId("card-1")
            .status(YankiWalletStatus.ACTIVE)
            .createdAt(now)
            .updatedAt(now)
            .build();

    // Act
    YankiWalletDocument document = mapper.toWalletDocument(domain);

    // Assert
    assertEquals("wallet-1", document.getId());
    assertEquals("DNI", document.getDocumentType());
    assertEquals("12345678", document.getDocumentNumber());
    assertEquals("999111222", document.getPhoneNumber());
    assertEquals("IMEI-1", document.getImei());
    assertEquals("john@mail.com", document.getEmail());
    assertEquals(new BigDecimal("100.00"), document.getBalance());
    assertEquals("card-1", document.getLinkedDebitCardId());
    assertEquals("ACTIVE", document.getStatus());
    assertEquals(now, document.getCreatedAt());
    assertEquals(now, document.getUpdatedAt());
  }

  @Test
  @DisplayName("Should be map null wallet enums as null document fields")
  void shouldBeMapNullWalletEnums() {
    // Arrange
    YankiWallet domain = YankiWallet.builder().phoneNumber("999111222").build();

    // Act
    YankiWalletDocument document = mapper.toWalletDocument(domain);

    // Assert
    assertNull(document.getDocumentType());
    assertNull(document.getStatus());
  }

  @Test
  @DisplayName("Should be map a wallet document to a domain object")
  void shouldBeMapWalletDocumentToDomain() {
    // Arrange
    YankiWalletDocument document = YankiWalletDocument.builder()
            .id("wallet-1")
            .documentType("PASSPORT")
            .documentNumber("AB123456")
            .phoneNumber("999111222")
            .imei("IMEI-1")
            .email("john@mail.com")
            .balance(new BigDecimal("25.00"))
            .linkedDebitCardId("card-1")
            .status("BLOCKED")
            .createdAt(now)
            .updatedAt(now)
            .build();

    // Act
    YankiWallet domain = mapper.toWalletDomain(document);

    // Assert
    assertEquals("wallet-1", domain.getId());
    assertEquals(YankiDocumentType.PASSPORT, domain.getDocumentType());
    assertEquals("AB123456", domain.getDocumentNumber());
    assertEquals("999111222", domain.getPhoneNumber());
    assertEquals("IMEI-1", domain.getImei());
    assertEquals("john@mail.com", domain.getEmail());
    assertEquals(new BigDecimal("25.00"), domain.getBalance());
    assertEquals("card-1", domain.getLinkedDebitCardId());
    assertEquals(YankiWalletStatus.BLOCKED, domain.getStatus());
    assertEquals(now, domain.getCreatedAt());
    assertEquals(now, domain.getUpdatedAt());
  }

  @Test
  @DisplayName("Should be map null wallet document enums as null domain fields")
  void shouldBeMapNullWalletDocumentEnums() {
    // Arrange
    YankiWalletDocument document = YankiWalletDocument.builder()
            .phoneNumber("999111222")
            .build();

    // Act
    YankiWallet domain = mapper.toWalletDomain(document);

    // Assert
    assertNull(domain.getDocumentType());
    assertNull(domain.getStatus());
  }

  @Test
  @DisplayName("Should be map a payment domain object to a document")
  void shouldBeMapPaymentDomainToDocument() {
    // Arrange
    YankiPayment domain = YankiPayment.builder()
            .id("payment-1")
            .transactionId("TX-1")
            .senderPhoneNumber("999111222")
            .receiverPhoneNumber("999333444")
            .senderDebitCardId("card-a")
            .receiverDebitCardId("card-b")
            .currency("PEN")
            .amount(new BigDecimal("30.00"))
            .description("Dinner")
            .status(YankiPaymentStatus.COMPLETED)
            .failureReason("none")
            .createdAt(now)
            .updatedAt(now)
            .build();

    // Act
    YankiPaymentDocument document = mapper.toPaymentDocument(domain);

    // Assert
    assertEquals("payment-1", document.getId());
    assertEquals("TX-1", document.getTransactionId());
    assertEquals("999111222", document.getSenderPhoneNumber());
    assertEquals("999333444", document.getReceiverPhoneNumber());
    assertEquals("card-a", document.getSenderDebitCardId());
    assertEquals("card-b", document.getReceiverDebitCardId());
    assertEquals("PEN", document.getCurrency());
    assertEquals(new BigDecimal("30.00"), document.getAmount());
    assertEquals("Dinner", document.getDescription());
    assertEquals("COMPLETED", document.getStatus());
    assertEquals("none", document.getFailureReason());
    assertEquals(now, document.getCreatedAt());
    assertEquals(now, document.getUpdatedAt());
  }

  @Test
  @DisplayName("Should be map a null payment status as a null document field")
  void shouldBeMapNullPaymentStatusToDocument() {
    // Arrange
    YankiPayment domain = YankiPayment.builder().transactionId("TX-1").build();

    // Act
    YankiPaymentDocument document = mapper.toPaymentDocument(domain);

    // Assert
    assertNull(document.getStatus());
  }

  @Test
  @DisplayName("Should be map a payment document to a domain object")
  void shouldBeMapPaymentDocumentToDomain() {
    // Arrange
    YankiPaymentDocument document = YankiPaymentDocument.builder()
            .id("payment-1")
            .transactionId("TX-1")
            .senderPhoneNumber("999111222")
            .receiverPhoneNumber("999333444")
            .senderDebitCardId("card-a")
            .receiverDebitCardId("card-b")
            .currency("USD")
            .amount(new BigDecimal("12.50"))
            .description("Lunch")
            .status("FAILED")
            .failureReason("Rejected")
            .createdAt(now)
            .updatedAt(now)
            .build();

    // Act
    YankiPayment domain = mapper.toPaymentDomain(document);

    // Assert
    assertEquals("payment-1", domain.getId());
    assertEquals("TX-1", domain.getTransactionId());
    assertEquals("999111222", domain.getSenderPhoneNumber());
    assertEquals("999333444", domain.getReceiverPhoneNumber());
    assertEquals("card-a", domain.getSenderDebitCardId());
    assertEquals("card-b", domain.getReceiverDebitCardId());
    assertEquals("USD", domain.getCurrency());
    assertEquals(new BigDecimal("12.50"), domain.getAmount());
    assertEquals("Lunch", domain.getDescription());
    assertEquals(YankiPaymentStatus.FAILED, domain.getStatus());
    assertEquals("Rejected", domain.getFailureReason());
    assertEquals(now, domain.getCreatedAt());
    assertEquals(now, domain.getUpdatedAt());
  }

  @Test
  @DisplayName("Should be map a null payment document status as a null domain field")
  void shouldBeMapNullPaymentDocumentStatus() {
    // Arrange
    YankiPaymentDocument document = YankiPaymentDocument.builder()
            .transactionId("TX-1")
            .build();

    // Act
    YankiPayment domain = mapper.toPaymentDomain(document);

    // Assert
    assertNull(domain.getStatus());
  }
}
