package com.pranith73.meridian.modules.onboarding.application;

import com.pranith73.meridian.modules.onboarding.application.request.CreateApplicationRequest;
import com.pranith73.meridian.modules.onboarding.domain.MerchantApplication;
import com.pranith73.meridian.shared.error.ResourceNotFoundException;

import java.util.UUID;

/**
 * Handles write operations for the Onboarding module.
 *
 * This service enforces onboarding workflow rules and coordinates
 * changes to MerchantApplication records. It delegates storage to
 * OnboardingApplicationRepository so it is not tied to any specific
 * storage technology.
 *
 * Allowed operations:
 *   - createApplication
 *   - submitApplication
 */
public class OnboardingApplicationService {

    private final OnboardingApplicationRepository repository;

    public OnboardingApplicationService(OnboardingApplicationRepository repository) {
        this.repository = repository;
    }

    // ---------------------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------------------

    /**
     * Opens a new onboarding application in DRAFT status.
     * Either merchantId or prospectReference must be provided on the request.
     */
    public MerchantApplication createApplication(CreateApplicationRequest request) {
        if (request == null) throw new IllegalArgumentException("request must not be null");

        MerchantApplication application = MerchantApplication.openDraft(
                request.getMerchantId(),
                request.getProspectReference(),
                request.getRequestedProducts()
        );

        return repository.save(application);
    }

    // ---------------------------------------------------------------------------
    // Submit
    // ---------------------------------------------------------------------------

    /**
     * Advances an existing DRAFT application to SUBMITTED.
     * Throws if the application is not found or is not in DRAFT status.
     */
    public MerchantApplication submitApplication(UUID applicationId) {
        if (applicationId == null) throw new IllegalArgumentException("applicationId must not be null");

        MerchantApplication application = repository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Onboarding application not found: " + applicationId));

        // submit() enforces the DRAFT -> SUBMITTED transition rule.
        application.submit();

        return repository.save(application);
    }
}
