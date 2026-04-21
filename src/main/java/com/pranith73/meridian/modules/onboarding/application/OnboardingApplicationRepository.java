package com.pranith73.meridian.modules.onboarding.application;

import com.pranith73.meridian.modules.onboarding.domain.MerchantApplication;

import java.util.Optional;
import java.util.UUID;

/**
 * Defines how Onboarding stores and retrieves MerchantApplication records.
 *
 * This interface keeps the application service independent of any storage
 * technology. Swap the implementation (in-memory, JDBC, etc.) without
 * touching service logic.
 */
public interface OnboardingApplicationRepository {

    /**
     * Saves a MerchantApplication. Inserts if new, updates if already present.
     * Returns the saved application.
     */
    MerchantApplication save(MerchantApplication application);

    /**
     * Finds a MerchantApplication by its applicationId.
     * Returns Optional.empty() if no record with that id exists.
     */
    Optional<MerchantApplication> findById(UUID applicationId);
}
