package com.pranith73.meridian.modules.onboarding.domain;

import com.pranith73.meridian.shared.file.DocumentReference;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a document requirement for a MerchantApplication.
 *
 * Onboarding owns the requirement and its fulfilment status.
 * The shared file boundary owns the actual file handling — storage,
 * AV scanning, and download access. A DocumentReference here is a
 * pointer to that file record, not the file content itself.
 *
 * State-changing methods return a new RequiredDocument rather than
 * mutating this one, keeping the object immutable.
 */
public class RequiredDocument {

    private final UUID requiredDocumentId;
    private final UUID applicationId;
    private final DocumentType documentType;
    private final RequiredDocumentStatus requirementStatus;

    // A reference to the most recently submitted file for this requirement.
    // Null until a document has been received.
    private final DocumentReference latestDocumentReference;

    // Present when the requirement is REJECTED or SCAN_BLOCKED.
    private final String rejectionReasonSummary;

    private final Instant receivedAt;   // null until a document is received
    private final Instant createdAt;
    private final Instant updatedAt;

    private RequiredDocument(UUID requiredDocumentId,
                              UUID applicationId,
                              DocumentType documentType,
                              RequiredDocumentStatus requirementStatus,
                              DocumentReference latestDocumentReference,
                              String rejectionReasonSummary,
                              Instant receivedAt,
                              Instant createdAt,
                              Instant updatedAt) {
        if (requiredDocumentId == null) throw new IllegalArgumentException("requiredDocumentId must not be null");
        if (applicationId == null)      throw new IllegalArgumentException("applicationId must not be null");
        if (documentType == null)       throw new IllegalArgumentException("documentType must not be null");
        if (requirementStatus == null)  throw new IllegalArgumentException("requirementStatus must not be null");
        if (createdAt == null)          throw new IllegalArgumentException("createdAt must not be null");
        if (updatedAt == null)          throw new IllegalArgumentException("updatedAt must not be null");

        this.requiredDocumentId = requiredDocumentId;
        this.applicationId = applicationId;
        this.documentType = documentType;
        this.requirementStatus = requirementStatus;
        this.latestDocumentReference = latestDocumentReference;
        this.rejectionReasonSummary = rejectionReasonSummary;
        this.receivedAt = receivedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Declares a new document requirement in PENDING status.
     * No document has been received yet.
     */
    public static RequiredDocument declareRequired(UUID applicationId, DocumentType documentType) {
        if (applicationId == null) throw new IllegalArgumentException("applicationId must not be null");
        if (documentType == null)  throw new IllegalArgumentException("documentType must not be null");

        Instant now = Instant.now();
        return new RequiredDocument(
                UUID.randomUUID(),
                applicationId,
                documentType,
                RequiredDocumentStatus.PENDING,
                null, null, null,
                now, now
        );
    }

    /**
     * Records that a document has been submitted for this requirement.
     * Only a PENDING or REJECTED requirement may receive a new document
     * — REJECTED allows resubmission after initial failure.
     */
    public RequiredDocument markReceived(DocumentReference documentReference) {
        if (requirementStatus != RequiredDocumentStatus.PENDING
                && requirementStatus != RequiredDocumentStatus.REJECTED) {
            throw new IllegalStateException(
                    "Only PENDING or REJECTED requirements can receive a document, current: " + requirementStatus);
        }
        if (documentReference == null) throw new IllegalArgumentException("documentReference must not be null");

        Instant now = Instant.now();
        return copy()
                .requirementStatus(RequiredDocumentStatus.RECEIVED)
                .latestDocumentReference(documentReference)
                .rejectionReasonSummary(null)
                .receivedAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Marks this requirement as waiting for an AV scan result.
     * Only a RECEIVED document may move to SCAN_PENDING.
     */
    public RequiredDocument markScanPending() {
        if (requirementStatus != RequiredDocumentStatus.RECEIVED) {
            throw new IllegalStateException(
                    "Only RECEIVED documents can move to SCAN_PENDING, current: " + requirementStatus);
        }
        Instant now = Instant.now();
        return copy()
                .requirementStatus(RequiredDocumentStatus.SCAN_PENDING)
                .updatedAt(now)
                .build();
    }

    /**
     * Marks this requirement as accepted.
     * Only RECEIVED or SCAN_PENDING documents may be accepted — those are the
     * two states where a document has been submitted and is under evaluation.
     * REJECTED documents must be resubmitted via markReceived before they can
     * be accepted. SCAN_BLOCKED documents cannot be accepted at all.
     * A document reference must also exist before acceptance is allowed.
     */
    public RequiredDocument markAccepted() {
        if (requirementStatus != RequiredDocumentStatus.RECEIVED
                && requirementStatus != RequiredDocumentStatus.SCAN_PENDING) {
            throw new IllegalStateException(
                    "Only RECEIVED or SCAN_PENDING documents can be accepted, current: " + requirementStatus);
        }
        if (latestDocumentReference == null) {
            throw new IllegalStateException("Cannot accept a requirement with no document reference");
        }
        Instant now = Instant.now();
        return copy()
                .requirementStatus(RequiredDocumentStatus.ACCEPTED)
                .rejectionReasonSummary(null)
                .updatedAt(now)
                .build();
    }

    /**
     * Marks this requirement as rejected by a reviewer.
     * A reason summary should explain what was wrong with the submission.
     */
    public RequiredDocument reject(String rejectionReasonSummary) {
        if (rejectionReasonSummary == null || rejectionReasonSummary.isBlank()) {
            throw new IllegalArgumentException("rejectionReasonSummary must not be blank");
        }
        Instant now = Instant.now();
        return copy()
                .requirementStatus(RequiredDocumentStatus.REJECTED)
                .rejectionReasonSummary(rejectionReasonSummary)
                .updatedAt(now)
                .build();
    }

    /**
     * Blocks this requirement because the AV scan flagged the submitted file.
     * A reason summary should describe the block.
     */
    public RequiredDocument blockDueToScan(String rejectionReasonSummary) {
        if (rejectionReasonSummary == null || rejectionReasonSummary.isBlank()) {
            throw new IllegalArgumentException("rejectionReasonSummary must not be blank");
        }
        Instant now = Instant.now();
        return copy()
                .requirementStatus(RequiredDocumentStatus.SCAN_BLOCKED)
                .rejectionReasonSummary(rejectionReasonSummary)
                .updatedAt(now)
                .build();
    }

    // ---------------------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------------------

    public UUID getRequiredDocumentId()                    { return requiredDocumentId; }
    public UUID getApplicationId()                         { return applicationId; }
    public DocumentType getDocumentType()                  { return documentType; }
    public RequiredDocumentStatus getRequirementStatus()   { return requirementStatus; }
    public DocumentReference getLatestDocumentReference()  { return latestDocumentReference; }
    public String getRejectionReasonSummary()              { return rejectionReasonSummary; }
    public Instant getReceivedAt()                         { return receivedAt; }
    public Instant getCreatedAt()                          { return createdAt; }
    public Instant getUpdatedAt()                          { return updatedAt; }

    // ---------------------------------------------------------------------------
    // Internal copy builder — keeps state-change methods readable
    // ---------------------------------------------------------------------------

    private Builder copy() {
        return new Builder(this);
    }

    private static class Builder {
        UUID requiredDocumentId;
        UUID applicationId;
        DocumentType documentType;
        RequiredDocumentStatus requirementStatus;
        DocumentReference latestDocumentReference;
        String rejectionReasonSummary;
        Instant receivedAt;
        Instant createdAt;
        Instant updatedAt;

        Builder(RequiredDocument source) {
            this.requiredDocumentId       = source.requiredDocumentId;
            this.applicationId            = source.applicationId;
            this.documentType             = source.documentType;
            this.requirementStatus        = source.requirementStatus;
            this.latestDocumentReference  = source.latestDocumentReference;
            this.rejectionReasonSummary   = source.rejectionReasonSummary;
            this.receivedAt               = source.receivedAt;
            this.createdAt                = source.createdAt;
            this.updatedAt                = source.updatedAt;
        }

        Builder requirementStatus(RequiredDocumentStatus v)       { this.requirementStatus = v; return this; }
        Builder latestDocumentReference(DocumentReference v)      { this.latestDocumentReference = v; return this; }
        Builder rejectionReasonSummary(String v)                  { this.rejectionReasonSummary = v; return this; }
        Builder receivedAt(Instant v)                             { this.receivedAt = v; return this; }
        Builder updatedAt(Instant v)                              { this.updatedAt = v; return this; }

        RequiredDocument build() {
            return new RequiredDocument(
                    requiredDocumentId, applicationId, documentType,
                    requirementStatus, latestDocumentReference,
                    rejectionReasonSummary, receivedAt, createdAt, updatedAt
            );
        }
    }
}
