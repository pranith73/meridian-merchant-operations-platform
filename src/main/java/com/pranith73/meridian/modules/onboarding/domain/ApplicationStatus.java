package com.pranith73.meridian.modules.onboarding.domain;

/**
 * Lifecycle states for a MerchantApplication.
 * These states belong to the onboarding workflow only —
 * they are not the same as MerchantStatus in Merchant Core.
 */
public enum ApplicationStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    NEEDS_INFO,
    APPROVED,
    REJECTED,
    ACTIVATED
}
