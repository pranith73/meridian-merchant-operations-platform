package com.pranith73.meridian.modules.merchantcore.application.request;

/**
 * Input data needed to create a new merchant.
 * The caller provides the merchant's legal name and the display name
 * shown in the platform UI. All other fields are assigned by the service.
 */
public class CreateMerchantRequest {

    private String legalName;
    private String displayName;

    public CreateMerchantRequest() {}

    public CreateMerchantRequest(String legalName, String displayName) {
        this.legalName = legalName;
        this.displayName = displayName;
    }

    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
