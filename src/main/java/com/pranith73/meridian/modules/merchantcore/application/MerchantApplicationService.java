package com.pranith73.meridian.modules.merchantcore.application;

import com.pranith73.meridian.modules.merchantcore.application.request.ChangeMerchantStatusRequest;
import com.pranith73.meridian.modules.merchantcore.application.request.CreateMerchantRequest;
import com.pranith73.meridian.modules.merchantcore.application.request.UpdateMerchantProfileRequest;
import com.pranith73.meridian.modules.merchantcore.domain.Merchant;
import com.pranith73.meridian.modules.merchantcore.domain.MerchantStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles all write operations for Merchant Core.
 *
 * This is the application service — it enforces business rules and
 * coordinates changes to merchant data. It does not talk to a database yet;
 * an in-memory map is used so the logic can be exercised and tested first.
 *
 * Allowed operations:
 *   - createMerchant
 *   - updateMerchantProfile
 *   - changeMerchantStatus
 */
public class MerchantApplicationService {

    // Temporary in-memory store. Replace with a real repository when ready.
    private final Map<UUID, Merchant> store = new HashMap<>();

    // ---------------------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------------------

    /**
     * Creates a new merchant and stores it with PENDING status.
     * Returns the newly created merchant.
     */
    public Merchant createMerchant(CreateMerchantRequest request) {
        validateNotBlank(request.getLegalName(), "legalName");
        validateNotBlank(request.getDisplayName(), "displayName");

        Merchant merchant = new Merchant();
        merchant.setMerchantId(UUID.randomUUID());
        merchant.setLegalName(request.getLegalName().trim());
        merchant.setDisplayName(request.getDisplayName().trim());
        merchant.setMerchantStatus(MerchantStatus.PENDING);
        merchant.setCreatedAt(Instant.now());
        merchant.setUpdatedAt(Instant.now());

        store.put(merchant.getMerchantId(), merchant);
        return merchant;
    }

    // ---------------------------------------------------------------------------
    // Update profile
    // ---------------------------------------------------------------------------

    /**
     * Updates the identity fields of an existing merchant.
     * Both legalName and displayName must be provided and non-blank.
     */
    public Merchant updateMerchantProfile(UpdateMerchantProfileRequest request) {
        validateNotNull(request.getMerchantId(), "merchantId");
        validateNotBlank(request.getLegalName(), "legalName");
        validateNotBlank(request.getDisplayName(), "displayName");

        Merchant merchant = requireMerchant(request.getMerchantId());

        merchant.setLegalName(request.getLegalName().trim());
        merchant.setDisplayName(request.getDisplayName().trim());
        merchant.setUpdatedAt(Instant.now());

        return merchant;
    }

    // ---------------------------------------------------------------------------
    // Change status
    // ---------------------------------------------------------------------------

    /**
     * Changes the merchant's status after verifying the transition is allowed.
     *
     * Allowed transitions (from the lifecycle rules):
     *   PENDING    -> ACTIVE
     *   ACTIVE     -> SUSPENDED
     *   SUSPENDED  -> ACTIVE
     *   ACTIVE     -> CLOSED
     *   SUSPENDED  -> CLOSED
     *
     * All other transitions are rejected.
     */
    public Merchant changeMerchantStatus(ChangeMerchantStatusRequest request) {
        validateNotNull(request.getMerchantId(), "merchantId");
        validateNotNull(request.getNewStatus(), "newStatus");

        Merchant merchant = requireMerchant(request.getMerchantId());

        MerchantStatus current = merchant.getMerchantStatus();
        MerchantStatus next = request.getNewStatus();

        if (!isTransitionAllowed(current, next)) {
            throw new IllegalStateException(
                "Status transition from " + current + " to " + next + " is not allowed.");
        }

        merchant.setMerchantStatus(next);
        merchant.setUpdatedAt(Instant.now());

        return merchant;
    }

    // ---------------------------------------------------------------------------
    // Transition rules
    // ---------------------------------------------------------------------------

    /**
     * Returns true only when the from->to transition is listed as allowed
     * in the Merchant Core lifecycle rules.
     */
    private boolean isTransitionAllowed(MerchantStatus from, MerchantStatus to) {
        switch (from) {
            case PENDING:
                return to == MerchantStatus.ACTIVE;
            case ACTIVE:
                return to == MerchantStatus.SUSPENDED || to == MerchantStatus.CLOSED;
            case SUSPENDED:
                return to == MerchantStatus.ACTIVE || to == MerchantStatus.CLOSED;
            case CLOSED:
                return false; // no transitions out of CLOSED
            default:
                return false;
        }
    }

    // ---------------------------------------------------------------------------
    // Guard helpers
    // ---------------------------------------------------------------------------

    /** Looks up a merchant by id and throws if not found. */
    private Merchant requireMerchant(UUID merchantId) {
        Merchant merchant = store.get(merchantId);
        if (merchant == null) {
            throw new IllegalArgumentException("Merchant not found: " + merchantId);
        }
        return merchant;
    }

    private void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }

    private void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null.");
        }
    }
}
