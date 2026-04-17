package com.pranith73.meridian.shared.audit;

/**
 * Shared abstraction for recording audit facts.
 *
 * Real implementations can be added later in infrastructure.
 */
public interface AuditSink {

    void record(AuditRecord auditRecord);
}