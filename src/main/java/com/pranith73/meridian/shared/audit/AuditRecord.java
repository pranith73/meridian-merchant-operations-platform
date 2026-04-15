package com.pranith73.meridian.shared.audit;

import java.time.Instant;

/**
 * Minimal audit fact for governed actions.
 *
 * This is a shared foundation model only.
 * It does not define persistence or transport yet.
 */
public record AuditRecord(
        String actionType,
        String subjectType,
        String subjectId,
        String actorId,
        String correlationId,
        Instant occurredAt,
        String detail
) {
}