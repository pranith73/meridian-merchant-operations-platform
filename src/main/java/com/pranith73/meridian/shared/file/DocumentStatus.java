package com.pranith73.meridian.shared.file;

/**
 * Lifecycle status for a governed document reference.
 *
 * The important rule is that files start quarantined
 * and only become usable after scanning and approval.
 */
public enum DocumentStatus {
    QUARANTINED,
    SCAN_PENDING,
    AVAILABLE,
    REJECTED
}