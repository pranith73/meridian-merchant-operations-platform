package com.pranith73.meridian.shared.outbox;

import java.time.Instant;

/**
 * Minimal outbox fact for later asynchronous follow-up work.
 *
 * This is a shared foundation model only.
 * It does not define persistence or dispatch yet.
 */
public record OutboxMessage(
        String eventType,
        String aggregateType,
        String aggregateId,
        String payload,
        String correlationId,
        Instant occurredAt
) {
}