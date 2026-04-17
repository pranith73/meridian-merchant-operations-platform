package com.pranith73.meridian.shared.api;

import java.time.Instant;

/**
 * Standard API error response shape.
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String correlationId
) {
}