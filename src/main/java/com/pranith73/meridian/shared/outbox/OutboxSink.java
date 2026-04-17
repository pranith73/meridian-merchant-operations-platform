package com.pranith73.meridian.shared.outbox;

/**
 * Shared abstraction for storing outbox facts.
 *
 * Real implementations can be added later in infrastructure.
 */
public interface OutboxSink {

    void save(OutboxMessage outboxMessage);
}