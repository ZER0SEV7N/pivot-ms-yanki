package com.pe.pivot.yanki.infrastructure.adapter.in.web.dto;

import java.time.Instant;
import lombok.Builder;

/**
 * Standardized API error response body matching yanki-api.yaml.
 */
@Builder
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}