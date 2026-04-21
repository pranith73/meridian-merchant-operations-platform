package com.pranith73.meridian.modules.onboarding.application.result;

import com.pranith73.meridian.modules.onboarding.domain.ActivationDecision;
import com.pranith73.meridian.modules.onboarding.domain.MerchantApplication;

/**
 * Returned from approveApplication and rejectApplication.
 * Carries both the updated application (with the new status) and the
 * ActivationDecision that was created as evidence of the governing decision.
 */
public class ApplicationDecisionResult {

    private final MerchantApplication application;
    private final ActivationDecision decision;

    public ApplicationDecisionResult(MerchantApplication application, ActivationDecision decision) {
        this.application = application;
        this.decision = decision;
    }

    public MerchantApplication getApplication() { return application; }
    public ActivationDecision getDecision()     { return decision; }
}
