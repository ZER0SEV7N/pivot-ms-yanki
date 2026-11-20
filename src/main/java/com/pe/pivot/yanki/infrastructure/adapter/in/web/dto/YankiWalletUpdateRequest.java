package com.pe.pivot.yanki.infrastructure.adapter.in.web.dto;

import com.pe.pivot.yanki.domain.model.YankiWalletStatus;
import jakarta.validation.constraints.Email;
import lombok.Builder;

/**
 * Request payload for updating Yanki wallet metadata or status.
 */
@Builder
public record YankiWalletUpdateRequest(
        @Email(message = "email must have a valid format") String email,
        String imei,
        YankiWalletStatus status
) {
}