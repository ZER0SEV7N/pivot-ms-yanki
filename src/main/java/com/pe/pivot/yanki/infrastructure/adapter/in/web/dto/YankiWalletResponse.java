package com.pe.pivot.yanki.infrastructure.adapter.in.web.dto;

import com.pe.pivot.yanki.domain.model.YankiDocumentType;
import com.pe.pivot.yanki.domain.model.YankiWalletStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;

/**
 * Response payload representing a Yanki mobile wallet.
 */
@Builder
public record YankiWalletResponse(
        String id,
        YankiDocumentType documentType,
        String documentNumber,
        String phoneNumber,
        String imei,
        String email,
        BigDecimal balance,
        String linkedDebitCardId,
        YankiWalletStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}