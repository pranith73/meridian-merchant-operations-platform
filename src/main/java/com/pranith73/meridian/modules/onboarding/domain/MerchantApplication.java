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

    // Status transitions and field updates will be added as explicit named methods
    // (e.g. submit(), startReview()) in later tasks. Direct setters are intentionally
    // absent so workflow state can only change through governed transition methods.
}
