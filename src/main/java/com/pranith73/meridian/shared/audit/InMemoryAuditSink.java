package com.pranith73.meridian.shared.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Temporary in-memory implementation of AuditSink.
 * Records are held in a list for the lifetime of the application.
 * Replace with a persistent implementation when the audit store is ready.
 */
public class InMemoryAuditSink implements AuditSink {

    private final List<AuditRecord> records = new ArrayList<>();

    @Override
    public void record(AuditRecord auditRecord) {
        records.add(auditRecord);
    }

    /** Returns a read-only view of all recorded audit entries. Useful in tests. */
    public List<AuditRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }
}
