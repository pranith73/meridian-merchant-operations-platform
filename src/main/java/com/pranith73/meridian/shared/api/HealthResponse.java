package com.pranith73.meridian.shared.api;

/**
 * Minimal response body for the application health endpoint.
 */
public record HealthResponse(
        String status,
        String application
) {
}