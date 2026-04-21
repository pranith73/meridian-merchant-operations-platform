package com.pranith73.meridian.modules.onboarding.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Root of the onboarding workflow for a prospective merchant.
 *
 * This class owns onboarding workflow state only. It may reference a
 * merchantId once a canonical merchant record has been reserved in
 * Merchant Core, but it does not own or modify merchant profile data.
 *
 * Before a Merchant Core record exists, the application is tracked by
 * prospectReference instead.
 */
public class MerchantApplication {

    private final UUID applicationId;

    // Set once a canonical Merchant Core record is reserved for this applicant.
    // Null until that reservation happens.
    private UUID merchantId;

    // Used to identify the applicant before a Merchant Core record is reserved.
    // At least one of merchantId or prospectReference must be present.
    private String prospectReference;

    // The products the merchant wants to be enabled for at activation.
    private final List<String> requestedProducts;

    private ApplicationStatus applicationStatus;
    private Instant submittedAt;
    private UUID assignedAnalystId;
    private LocalDate targetGoLiveDate;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Private constructor — use the openDraft factory method to create instances.
     * Enforces that required fields are present before the object is used.
     */
    private MerchantApplication(UUID applicationId,
                                 UUID merchantId,
                                 String prospectReference,
                                 List<String> requestedProducts,
                                 ApplicationStatus applicationStatus,
                                 Instant createdAt,
                                 Instant updatedAt) {
        if (applicationId == null) throw new IllegalArgumentException("applicationId must not be null");
        if (requestedProducts == null) throw new IllegalArgumentException("requestedProducts must not be null");
        if (applicationStatus == null) throw new IllegalArgumentException("applicationStatus must not be null");
        if (createdAt == null) throw new IllegalArgumentException("createdAt must not be null");
        if (updatedAt == null) throw new IllegalArgumentException("updatedAt must not be null");

        // At least one identifier must be present so the application can be traced.
        if (merchantId == null && (prospectReference == null || prospectReference.isBlank())) {
            throw new IllegalArgumentException("Either merchantId or prospectReference must be provided");
        }

        this.applicationId = applicationId;
        this.merchantId = merchantId;
        this.prospectReference = prospectReference;
        // Defensive copy so callers cannot mutate the internal list.
        this.requestedProducts = new ArrayList<>(requestedProducts);
        this.applicationStatus = applicationStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Creates a new MerchantApplication in DRAFT status.
     *
     * Provide merchantId if a Merchant Core record already exists.
     * Provide prospectReference if the Merchant Core record has not been
     * reserved yet. At least one must be non-null.
     */
    public static MerchantApplication openDraft(UUID merchantId,
                                                 String prospectReference,
                                                 List<String> requestedProducts) {
        Instant now = Instant.now();
        return new MerchantApplication(
                UUID.randomUUID(),
                merchantId,
                prospectReference,
                requestedProducts != null ? requestedProducts : new ArrayList<>(),
                ApplicationStatus.DRAFT,
                now,
                now
        );
    }

    // ---------------------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------------------

    public UUID getApplicationId() { return applicationId; }

    public UUID getMerchantId() { return merchantId; }

    public String getProspectReference() { return prospectReference; }

    /** Returns a read-only view of the requested products. */
    public List<String> getRequestedProducts() {
        return Collections.unmodifiableList(requestedProducts);
    }

    public ApplicationStatus getApplicationStatus() { return applicationStatus; }

    public Instant getSubmittedAt() { return submittedAt; }

    public UUID getAssignedAnalystId() { return assignedAnalystId; }

    public LocalDate getTargetGoLiveDate() { return targetGoLiveDate; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    // ---------------------------------------------------------------------------
    // Workflow transitions
    //
    // Status changes must go through named methods like submit() rather than
    // public setters. This ensures that every transition can enforce its own
    // preconditions and that no caller can jump the workflow to an arbitrary state.
    // ---------------------------------------------------------------------------

    /**
     * Advances this application from DRAFT to SUBMITTED.
     * Calling submit() on an application that is already SUBMITTED or in any
     * later state is rejected — submission is a one-time governed action.
     */
    public void submit() {
        if (applicationStatus != ApplicationStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only a DRAFT application can be submitted, current status: " + applicationStatus);
        }
        Instant now = Instant.now();
        this.applicationStatus = ApplicationStatus.SUBMITTED;
        this.submittedAt = now;
        this.updatedAt = now;
    }

    /**
     * Begins formal review of this application and assigns it to an analyst.
     * Allowed from SUBMITTED (first review pass) or NEEDS_INFO (re-review after
     * the applicant has responded). All other statuses are rejected so the
     * workflow cannot skip or loop backwards outside these two entry points.
     */
    public void startReview(UUID assignedAnalystId) {
        if (assignedAnalystId == null) throw new IllegalArgumentException("assignedAnalystId must not be null");

        if (applicationStatus != ApplicationStatus.SUBMITTED
                && applicationStatus != ApplicationStatus.NEEDS_INFO) {
            throw new IllegalStateException(
                    "Review can only be started from SUBMITTED or NEEDS_INFO, current status: " + applicationStatus);
        }
        this.assignedAnalystId = assignedAnalystId;
        this.applicationStatus = ApplicationStatus.UNDER_REVIEW;
        this.updatedAt = Instant.now();
    }

    /**
     * Flags this application as needing additional information from the applicant.
     * Only allowed while the application is UNDER_REVIEW — the analyst must be
     * actively reviewing before they can request changes.
     * The reason is passed at the service layer for logging or notification;
     * this method updates workflow state only.
     */
    public void requestChanges(String reasonSummary) {
        if (reasonSummary == null || reasonSummary.isBlank()) {
            throw new IllegalArgumentException("reasonSummary must not be blank");
        }
        if (applicationStatus != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Changes can only be requested while UNDER_REVIEW, current status: " + applicationStatus);
        }
        this.applicationStatus = ApplicationStatus.NEEDS_INFO;
        this.updatedAt = Instant.now();
    }

    /**
     * Records an approval decision for this application.
     * Only allowed while the application is UNDER_REVIEW — the analyst must
     * have been actively reviewing before a positive decision can be recorded.
     * Approval is a governed transition; it cannot be set directly on the status field.
     */
    public void approve() {
        if (applicationStatus != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Only an UNDER_REVIEW application can be approved, current status: " + applicationStatus);
        }
        this.applicationStatus = ApplicationStatus.APPROVED;
        this.updatedAt = Instant.now();
    }

    /**
     * Records a rejection decision for this application.
     * Only allowed while the application is UNDER_REVIEW — the analyst must
     * have been actively reviewing before a negative decision can be recorded.
     * Rejection is a governed transition; it cannot be set directly on the status field.
     */
    public void reject() {
        if (applicationStatus != ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Only an UNDER_REVIEW application can be rejected, current status: " + applicationStatus);
        }
        this.applicationStatus = ApplicationStatus.REJECTED;
        this.updatedAt = Instant.now();
    }
}
