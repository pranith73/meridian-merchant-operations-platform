package com.pranith73.meridian.modules.onboarding.application.request;

import java.util.UUID;

/**
 * Input data needed to approve or reject an onboarding application.
 * Used for both approveApplication and rejectApplication so the two
 * service methods share the same input shape.
 */
public class DecisionRequest {

    private UUID applicationId;
    private UUID decidedBy;
    private String decisionReasonSummary;

    public DecisionRequest() {}

    public DecisionRequest(UUID applicationId, UUID decidedBy, String decisionReasonSummary) {
        this.applicationId = applicationId;
        this.decidedBy = decidedBy;
        this.decisionReasonSummary = decisionReasonSummary;
    }

    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }

    public UUID getDecidedBy() { return decidedBy; }
    public void setDecidedBy(UUID decidedBy) { this.decidedBy = decidedBy; }

    public String getDecisionReasonSummary() { return decisionReasonSummary; }
    public void setDecisionReasonSummary(String decisionReasonSummary) { this.decisionReasonSummary = decisionReasonSummary; }
}
