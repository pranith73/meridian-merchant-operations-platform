package com.pranith73.meridian.modules.onboarding.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * A formal review checkpoint for a MerchantApplication.
 *
 * A single application may have multiple reviews of different types
 * (e.g. BUSINESS_PROFILE and RISK_REVIEW). Each review is independent
 * and references the application only by applicationId.
 *
 * Use open(...) to start a new checkpoint in PENDING status.
 * Use recordOutcome(...) on that instance to complete the same checkpoint
 * with a final outcome. The result is a new immutable instance that preserves
 * the original reviewId and createdAt.
 */
public class ApplicationReview {

    private final UUID reviewId;
    private final UUID applicationId;
    private final ReviewType reviewType;
    private final ReviewStatus reviewStatus;
    private final UUID reviewerId;       // null when the review is still pending
    private final String reviewNotesSummary; // null when the review is still pending
    private final Instant decidedAt;     // null when the review is still pending
    private final Instant createdAt;

    private ApplicationReview(UUID reviewId,
                               UUID applicationId,
                               ReviewType reviewType,
                               ReviewStatus reviewStatus,
                               UUID reviewerId,
                               String reviewNotesSummary,
                               Instant decidedAt,
                               Instant createdAt) {
        if (reviewId == null)      throw new IllegalArgumentException("reviewId must not be null");
        if (applicationId == null) throw new IllegalArgumentException("applicationId must not be null");
        if (reviewType == null)    throw new IllegalArgumentException("reviewType must not be null");
        if (reviewStatus == null)  throw new IllegalArgumentException("reviewStatus must not be null");
        if (createdAt == null)     throw new IllegalArgumentException("createdAt must not be null");

        this.reviewId = reviewId;
        this.applicationId = applicationId;
        this.reviewType = reviewType;
        this.reviewStatus = reviewStatus;
        this.reviewerId = reviewerId;
        this.reviewNotesSummary = reviewNotesSummary;
        this.decidedAt = decidedAt;
        this.createdAt = createdAt;
    }

    /**
     * Opens a new review checkpoint in PENDING status.
     * Reviewer, notes, and decision time are not known yet.
     */
    public static ApplicationReview open(UUID applicationId, ReviewType reviewType) {
        if (applicationId == null) throw new IllegalArgumentException("applicationId must not be null");
        if (reviewType == null)    throw new IllegalArgumentException("reviewType must not be null");

        Instant now = Instant.now();
        return new ApplicationReview(
                UUID.randomUUID(),
                applicationId,
                reviewType,
                ReviewStatus.PENDING,
                null,
                null,
                null,
                now
        );
    }

    /**
     * Completes this review checkpoint and returns a new immutable instance
     * with the outcome applied.
     *
     * Only a PENDING review may be completed — calling this on an already
     * decided review is rejected. PENDING is not a valid outcome status.
     * The returned instance carries the same reviewId and createdAt as this one.
     */
    public ApplicationReview recordOutcome(ReviewStatus reviewStatus,
                                            UUID reviewerId,
                                            String reviewNotesSummary) {
        if (this.reviewStatus != ReviewStatus.PENDING) {
            throw new IllegalStateException(
                    "Only a PENDING review can be completed, current status: " + this.reviewStatus);
        }
        if (reviewStatus == null) throw new IllegalArgumentException("reviewStatus must not be null");
        if (reviewStatus == ReviewStatus.PENDING) {
            throw new IllegalArgumentException("PENDING is not a valid outcome status");
        }
        if (reviewerId == null)   throw new IllegalArgumentException("reviewerId must not be null");

        return new ApplicationReview(
                this.reviewId,
                this.applicationId,
                this.reviewType,
                reviewStatus,
                reviewerId,
                reviewNotesSummary,
                Instant.now(),
                this.createdAt
        );
    }

    public UUID getReviewId()             { return reviewId; }
    public UUID getApplicationId()        { return applicationId; }
    public ReviewType getReviewType()     { return reviewType; }
    public ReviewStatus getReviewStatus() { return reviewStatus; }
    public UUID getReviewerId()           { return reviewerId; }
    public String getReviewNotesSummary() { return reviewNotesSummary; }
    public Instant getDecidedAt()         { return decidedAt; }
    public Instant getCreatedAt()         { return createdAt; }
}
