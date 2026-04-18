package com.pranith73.meridian.modules.merchantcore.application.request;

import java.util.UUID;

/**
 * Input data needed to update a merchant's identity profile.
 * Only the fields a caller is allowed to change are included here.
 * merchantId identifies which merchant to update.
 */
public class UpdateMerchantProfileRequest {

    private UUID merchantId;
    private String legalName;
    private String displayName;

    public UpdateMerchantProfileRequest() {}

    public UpdateMerchantProfileRequest(UUID merchantId, String legalName, String displayName) {
        this.merchantId = merchantId;
        this.legalName = legalName;
        this.displayName = displayName;
    }

    public UUID getMerchantId() { return merchantId; }
    public void setMerchantId(UUID merchantId) { this.merchantId = merchantId; }

    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
