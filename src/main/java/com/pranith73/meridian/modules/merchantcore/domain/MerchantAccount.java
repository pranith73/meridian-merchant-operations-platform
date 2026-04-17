package com.pranith73.meridian.modules.merchantcore.domain;

import java.util.UUID;

/** Represents the acquiring account assigned to a merchant. */
public class MerchantAccount {

    private UUID merchantAccountId;
    private UUID merchantId;
    private String processorName;
    private String processorMerchantRef; // merchant identifier issued by the processor
    private String accountStatus; // e.g. ACTIVE, FROZEN, CLOSED

    public MerchantAccount() {}

    public UUID getMerchantAccountId() { return merchantAccountId; }
    public void setMerchantAccountId(UUID merchantAccountId) { this.merchantAccountId = merchantAccountId; }

    public UUID getMerchantId() { return merchantId; }
    public void setMerchantId(UUID merchantId) { this.merchantId = merchantId; }

    public String getProcessorName() { return processorName; }
    public void setProcessorName(String processorName) { this.processorName = processorName; }

    public String getProcessorMerchantRef() { return processorMerchantRef; }
    public void setProcessorMerchantRef(String processorMerchantRef) { this.processorMerchantRef = processorMerchantRef; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
}
