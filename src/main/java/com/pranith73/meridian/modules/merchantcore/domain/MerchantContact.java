package com.pranith73.meridian.modules.merchantcore.domain;

import java.util.UUID;

/** Primary contact person associated with a merchant. */
public class MerchantContact {

    private UUID contactId;
    private UUID merchantId;
    private String fullName;
    private String email;
    private String phone;
    private String contactRole; // e.g. OWNER, ADMIN, BILLING
    private boolean active;

    public MerchantContact() {}

    public UUID getContactId() { return contactId; }
    public void setContactId(UUID contactId) { this.contactId = contactId; }

    public UUID getMerchantId() { return merchantId; }
    public void setMerchantId(UUID merchantId) { this.merchantId = merchantId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getContactRole() { return contactRole; }
    public void setContactRole(String contactRole) { this.contactRole = contactRole; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
