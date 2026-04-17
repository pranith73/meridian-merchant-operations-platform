package com.pranith73.meridian.modules.merchantcore.domain;

import java.util.UUID;

/** Funding destination and payout cadence for a merchant's settlements. */
public class SettlementProfile {

    private UUID settlementProfileId;
    private UUID merchantId;
    private String fundingFrequency; // e.g. DAILY, WEEKLY, MONTHLY
    private String payoutMethodSummary; // human-readable payout destination (e.g. "Chase ****4321")
    private boolean active;

    public SettlementProfile() {}

    public UUID getSettlementProfileId() { return settlementProfileId; }
    public void setSettlementProfileId(UUID settlementProfileId) { this.settlementProfileId = settlementProfileId; }

    public UUID getMerchantId() { return merchantId; }
    public void setMerchantId(UUID merchantId) { this.merchantId = merchantId; }

    public String getFundingFrequency() { return fundingFrequency; }
    public void setFundingFrequency(String fundingFrequency) { this.fundingFrequency = fundingFrequency; }

    public String getPayoutMethodSummary() { return payoutMethodSummary; }
    public void setPayoutMethodSummary(String payoutMethodSummary) { this.payoutMethodSummary = payoutMethodSummary; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
