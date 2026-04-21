package com.pranith73.meridian.modules.onboarding.infrastructure;

import com.pranith73.meridian.modules.onboarding.application.OnboardingApplicationRepository;
import com.pranith73.meridian.modules.onboarding.domain.MerchantApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Temporary in-memory implementation of OnboardingApplicationRepository.
 *
 * Applications are held in a plain HashMap for the duration of the
 * application's lifetime. This lets the service layer and its workflow
 * rules be developed and tested before a real database is wired in.
 *
 * When real persistence is ready, implement OnboardingApplicationRepository
 * using JDBC or another mechanism and replace this class wherever it is
 * wired up. Nothing in the service layer needs to change.
 */
public class InMemoryOnboardingApplicationRepository implements OnboardingApplicationRepository {

    private final Map<UUID, MerchantApplication> store = new HashMap<>();

    @Override
    public MerchantApplication save(MerchantApplication application) {
        store.put(application.getApplicationId(), application);
        return application;
    }

    @Override
    public Optional<MerchantApplication> findById(UUID applicationId) {
        return Optional.ofNullable(store.get(applicationId));
    }
}
