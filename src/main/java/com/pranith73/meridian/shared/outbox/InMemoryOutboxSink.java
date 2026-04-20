package com.pranith73.meridian.shared.outbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Temporary in-memory implementation of OutboxSink.
 * Messages are held in a list for the lifetime of the application.
 * Replace with a persistent implementation when the outbox store is ready.
 */
public class InMemoryOutboxSink implements OutboxSink {

    private final List<OutboxMessage> messages = new ArrayList<>();

    @Override
    public void save(OutboxMessage outboxMessage) {
        messages.add(outboxMessage);
    }

    /** Returns a read-only view of all saved outbox messages. Useful in tests. */
    public List<OutboxMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }
}
