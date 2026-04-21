package com.pranith73.meridian.modules.onboarding.domain;

/**
 * Tracks the fulfilment state of a document requirement in Onboarding.
 * This is separate from DocumentStatus in the shared file boundary,
 * which tracks the storage and scan state of the file itself.
 */
public enum RequiredDocumentStatus {
    PENDING,       // requirement declared, no document received yet
    RECEIVED,      // document submitted, not yet scanned
    SCAN_PENDING,  // document handed to AV scan, awaiting result
    ACCEPTED,      // document passed review and scan
    REJECTED,      // document rejected by a reviewer
    SCAN_BLOCKED   // document blocked because AV scan flagged it
}
