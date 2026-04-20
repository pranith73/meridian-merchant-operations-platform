package com.pranith73.meridian.modules.onboarding.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * The governed business decision for a MerchantApplication.
 *
 * An ActivationDecision is immutable once created — a decision cannot be
 * revised in place. If a decision needs to change, the workflow must
 * produce a new decision through the appropriate process.
 *
 * Use approve(...) or reject(...) to record the outcome.
 */
public class ActivationDecision {

    private final UUID decisionId;
    private final UUID applicationId;
    private final DecisionType decisionType;
    private final String decisionReasonSummary;
    private final UUID decidedBy;
    private final Instant decidedAt;

    private ActivationDecision(UUID decisionId,
                                UUID applicationId,
                                DecisionType decisionType,
                                String decisionReasonSummary,
                                UUID decidedBy,
                                Instant decidedAt) {
        if (decisionId == null)    throw new IllegalArgumentException("decisionId must not be null");
        if (applicationId == null) throw new IllegalArgumentException("applicationId must not be null");
        if (decisionType == null)  throw new IllegalArgumentException("decisionType must not be null");
        if (decidedBy == null)     throw new IllegalArgumentException("decidedBy must not be null");
        if (decidedAt == null)     throw new IllegalArgumentException("decidedAt must not be null");

        this.decisionId = decisionId;
        this.applicationId = applicationId;
        this.decisionType = decisionType;
        this.decisionReasonSummary = decisionReasonSummary;
        this.decidedBy = decidedBy;
        this.decidedAt = decidedAt;
    }

    /** Records an approval decision for the given application. */
    public static ActivationDecision approve(UUID applicationId,
                                              String decisionReasonSummary,
                                              UUID decidedBy) {
        return new ActivationDecision(
                UUID.randomUUID(),
                applicationId,
                DecisionType.APPROVED,
                decisionReasonSummary,
                decidedBy,
                Instant.now()
        );
    }

    /** Records a rejection decision for the given application. */
    public static ActivationDecision reject(UUID applicationId,
                                             String decisionReasonSummary,
                                             UUID decidedBy) {
        return new ActivationDecision(
                UUID.randomUUID(),
                applicationId,
                DecisionType.REJECTED,
                decisionReasonSummary,
                decidedBy,
                Instant.now()
        );
    }

    public UUID getDecisionId()               { return decisionId; }
    public UUID getApplicationId()            { return applicationId; }
    public DecisionType getDecisionType()     { return decisionType; }
    public String getDecisionReasonSummary()  { return decisionReasonSummary; }
    public UUID getDecidedBy()                { return decidedBy; }
    public Instant getDecidedAt()             { return decidedAt; }
}
