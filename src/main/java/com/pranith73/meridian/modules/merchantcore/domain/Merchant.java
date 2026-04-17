package com.pranith73.meridian.modules.merchantcore.domain;

import java.time.Instant;
import java.util.UUID;

/** Root identity record for a merchant in the platform. */
public class Merchant {

    private UUID merchantId;
    private String legalName;
    private String displayName;
    private String merchantStatus; // e.g. ACTIVE, SUSPENDED, CLOSED
    private Instant createdAt;
    private Instant updatedAt;

    public Merchant() {}

    public UUID getMerchantId() { return merchantId; }
    public void setMerchantId(UUID merchantId) { this.merchantId = merchantId; }

    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getMerchantStatus() { return merchantStatus; }
    public void setMerchantStatus(String merchantStatus) { this.merchantStatus = merchantStatus; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
