package com.pranith73.meridian.modules.merchantcore.application.request;

import com.pranith73.meridian.modules.merchantcore.domain.MerchantStatus;

import java.util.UUID;

/**
 * Input data needed to change a merchant's status.
 * The service will validate that the transition from the current
 * status to the requested status is allowed before applying the change.
 */
public class ChangeMerchantStatusRequest {

    private UUID merchantId;
    private MerchantStatus newStatus;

    public ChangeMerchantStatusRequest() {}

    public ChangeMerchantStatusRequest(UUID merchantId, MerchantStatus newStatus) {
        this.merchantId = merchantId;
        this.newStatus = newStatus;
    }

    public UUID getMerchantId() { return merchantId; }
    public void setMerchantId(UUID merchantId) { this.merchantId = merchantId; }

    public MerchantStatus getNewStatus() { return newStatus; }
    public void setNewStatus(MerchantStatus newStatus) { this.newStatus = newStatus; }
}
