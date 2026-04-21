package com.pranith73.meridian.modules.onboarding.application;

import com.pranith73.meridian.modules.onboarding.application.request.CreateApplicationRequest;
import com.pranith73.meridian.modules.onboarding.application.request.DecisionRequest;
import com.pranith73.meridian.modules.onboarding.application.request.RequestChangesRequest;
import com.pranith73.meridian.modules.onboarding.application.result.ApplicationDecisionResult;
import com.pranith73.meridian.modules.onboarding.domain.ActivationDecision;
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
 *   - startReview
 *   - requestChanges
 *   - approveApplication
 *   - rejectApplication
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

    // ---------------------------------------------------------------------------
    // Start review
    // ---------------------------------------------------------------------------

    /**
     * Assigns an analyst and moves the application into UNDER_REVIEW.
     * Allowed from SUBMITTED or NEEDS_INFO.
     * Throws if the application is not found or the transition is not allowed.
     */
    public MerchantApplication startReview(UUID applicationId, UUID assignedAnalystId) {
        if (applicationId == null)     throw new IllegalArgumentException("applicationId must not be null");
        if (assignedAnalystId == null) throw new IllegalArgumentException("assignedAnalystId must not be null");

        MerchantApplication application = requireApplication(applicationId);

        // startReview() enforces SUBMITTED/NEEDS_INFO -> UNDER_REVIEW.
        application.startReview(assignedAnalystId);

        return repository.save(application);
    }

    // ---------------------------------------------------------------------------
    // Request changes
    // ---------------------------------------------------------------------------

    /**
     * Flags the application as NEEDS_INFO and records the reason the analyst
     * requires additional information from the applicant.
     * Only allowed while the application is UNDER_REVIEW.
     * Throws if the application is not found or the transition is not allowed.
     */
    public MerchantApplication requestChanges(RequestChangesRequest request) {
        if (request == null)                                          throw new IllegalArgumentException("request must not be null");
        if (request.getApplicationId() == null)                       throw new IllegalArgumentException("applicationId must not be null");
        if (request.getReasonSummary() == null || request.getReasonSummary().isBlank()) throw new IllegalArgumentException("reasonSummary must not be blank");

        MerchantApplication application = requireApplication(request.getApplicationId());

        // requestChanges() enforces UNDER_REVIEW -> NEEDS_INFO.
        application.requestChanges(request.getReasonSummary());

        return repository.save(application);
    }

    // ---------------------------------------------------------------------------
    // Approve
    // ---------------------------------------------------------------------------

    /**
     * Records an approval decision for an UNDER_REVIEW application.
     * Creates ActivationDecision evidence and returns both the updated
     * application and the decision together in ApplicationDecisionResult.
     * ActivationDecision is not persisted separately yet; that comes later.
     */
    public ApplicationDecisionResult approveApplication(DecisionRequest request) {
        validateDecisionRequest(request);

        MerchantApplication application = requireApplication(request.getApplicationId());

        // approve() enforces the UNDER_REVIEW -> APPROVED transition.
        application.approve();

        ActivationDecision decision = ActivationDecision.approve(
                application.getApplicationId(),
                request.getDecisionReasonSummary(),
                request.getDecidedBy()
        );

        MerchantApplication saved = repository.save(application);
        return new ApplicationDecisionResult(saved, decision);
    }

    // ---------------------------------------------------------------------------
    // Reject
    // ---------------------------------------------------------------------------

    /**
     * Records a rejection decision for an UNDER_REVIEW application.
     * Creates ActivationDecision evidence and returns both the updated
     * application and the decision together in ApplicationDecisionResult.
     * ActivationDecision is not persisted separately yet; that comes later.
     */
    public ApplicationDecisionResult rejectApplication(DecisionRequest request) {
        validateDecisionRequest(request);

        MerchantApplication application = requireApplication(request.getApplicationId());

        // reject() enforces the UNDER_REVIEW -> REJECTED transition.
        application.reject();

        ActivationDecision decision = ActivationDecision.reject(
                application.getApplicationId(),
                request.getDecisionReasonSummary(),
                request.getDecidedBy()
        );

        MerchantApplication saved = repository.save(application);
        return new ApplicationDecisionResult(saved, decision);
    }

    // ---------------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------------

    private MerchantApplication requireApplication(UUID applicationId) {
        return repository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Onboarding application not found: " + applicationId));
    }

    private void validateDecisionRequest(DecisionRequest request) {
        if (request == null) throw new IllegalArgumentException("request must not be null");
        if (request.getApplicationId() == null) throw new IllegalArgumentException("applicationId must not be null");
        if (request.getDecidedBy() == null) throw new IllegalArgumentException("decidedBy must not be null");
        if (request.getDecisionReasonSummary() == null || request.getDecisionReasonSummary().isBlank()) {
            throw new IllegalArgumentException("decisionReasonSummary must not be blank");
        }
    }
}
