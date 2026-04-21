package com.pranith73.meridian.modules.onboarding.application.request;

import java.util.UUID;

/**
 * Input data needed to request changes on an onboarding application.
 * The reasonSummary explains to the applicant what information is missing
 * or what needs to be corrected before review can continue.
 */
public class RequestChangesRequest {

    private UUID applicationId;
    private String reasonSummary;

    public RequestChangesRequest() {}

    public RequestChangesRequest(UUID applicationId, String reasonSummary) {
        this.applicationId = applicationId;
        this.reasonSummary = reasonSummary;
    }

    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }

    public String getReasonSummary() { return reasonSummary; }
    public void setReasonSummary(String reasonSummary) { this.reasonSummary = reasonSummary; }
}
