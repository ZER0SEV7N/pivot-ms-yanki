package com.pe.pivot.yanki.application.support;

import com.pe.pivot.yanki.domain.exception.BusinessRuleException;
import com.pe.pivot.yanki.domain.model.YankiDocumentType;
import com.pe.pivot.yanki.domain.model.YankiWallet;
import com.pe.pivot.yanki.domain.model.YankiWalletStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Domain Initializer validating identity document rules and setting default wallet state (SRP).
 */
@Component
public class YankiWalletDomainInitializer {

  // Validates the identity document type and number according to business rules.
  private static final String DNI_REGEX = "^\\d{8}$";
  private static final String FOREIGN_DOC_REGEX = "^[A-Za-z0-9]{6,12}$";

  /**
   * Validates document format and populates default fields on a new YankiWallet.
   *
   * @param wallet the incoming YankiWallet
   */
  public void initialize(YankiWallet wallet) {
    validateDocument(wallet.getDocumentType(), wallet.getDocumentNumber());
    wallet.setPhoneNumber(wallet.getPhoneNumber().trim());
    wallet.setEmail(wallet.getEmail().trim().toLowerCase());
    wallet.setImei(wallet.getImei().trim());
    wallet.setBalance(Optional.ofNullable(wallet.getBalance()).orElse(BigDecimal.ZERO));
    wallet.setStatus(YankiWalletStatus.ACTIVE);
    Instant now = Instant.now();
    wallet.setCreatedAt(now);
    wallet.setUpdatedAt(now);
  }

  /**
   * Validates the document number based on its type (DNI, CE, PASSPORT) and throws a
   *
   * @param type - the type of identification document (DNI, CE, PASSPORT)
   * @param number - the identification document number to validate
   * @throws BusinessRuleException if the document number does not conform to the expected format
   *                               for the given document type
   */
  private void validateDocument(YankiDocumentType type, String number) {
    // Validate that the document number is not null or empty
    if (type == YankiDocumentType.DNI && !number.matches(DNI_REGEX)) {
      throw new BusinessRuleException("DNI must contain exactly 8 numeric digits.");
    }
    // Validate that the document number is not null or empty for CE and PASSPORT types
    if ((type == YankiDocumentType.CEX || type == YankiDocumentType.PASSPORT)
            && !number.matches(FOREIGN_DOC_REGEX)) {
      throw new BusinessRuleException(
              type + " document number must be 6 to 12 alphanumeric characters.");
    }
  }
}
