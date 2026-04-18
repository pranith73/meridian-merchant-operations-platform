package com.pranith73.meridian.modules.merchantcore.application;

import com.pranith73.meridian.modules.merchantcore.application.request.ChangeMerchantStatusRequest;
import com.pranith73.meridian.modules.merchantcore.application.request.CreateMerchantRequest;
import com.pranith73.meridian.modules.merchantcore.application.request.SearchMerchantsRequest;
import com.pranith73.meridian.modules.merchantcore.application.request.UpdateMerchantProfileRequest;
import com.pranith73.meridian.modules.merchantcore.domain.Merchant;
import com.pranith73.meridian.modules.merchantcore.domain.MerchantStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles all read and write operations for Merchant Core.
 *
 * This is the application service — it enforces business rules and
 * coordinates changes to merchant data. It delegates storage to
 * MerchantRepository so it is not tied to any specific storage technology.
 *
 * Allowed operations:
 *   - createMerchant
 *   - updateMerchantProfile
 *   - changeMerchantStatus
 *   - getMerchantById
 *   - searchMerchants
 */
public class MerchantApplicationService {

    private final MerchantRepository merchantRepository;

    /**
     * The repository is passed in (injected) so the service works with any
     * implementation — in-memory today, a real database tomorrow.
     */
    public MerchantApplicationService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    // ---------------------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------------------

    /**
     * Creates a new merchant and stores it with PENDING status.
     * Returns the newly created merchant.
     */
    public Merchant createMerchant(CreateMerchantRequest request) {
        validateNotNull(request, "request");
        validateNotBlank(request.getLegalName(), "legalName");
        validateNotBlank(request.getDisplayName(), "displayName");

        Merchant merchant = new Merchant();
        merchant.setMerchantId(UUID.randomUUID());
        merchant.setLegalName(request.getLegalName().trim());
        merchant.setDisplayName(request.getDisplayName().trim());
        merchant.setMerchantStatus(MerchantStatus.PENDING);
        merchant.setCreatedAt(Instant.now());
        merchant.setUpdatedAt(Instant.now());

        return merchantRepository.save(merchant);
    }

    // ---------------------------------------------------------------------------
    // Update profile
    // ---------------------------------------------------------------------------

    /**
     * Updates the identity fields of an existing merchant.
     * Both legalName and displayName must be provided and non-blank.
     */
    public Merchant updateMerchantProfile(UpdateMerchantProfileRequest request) {
        validateNotNull(request, "request");
        validateNotNull(request.getMerchantId(), "merchantId");
        validateNotBlank(request.getLegalName(), "legalName");
        validateNotBlank(request.getDisplayName(), "displayName");

        Merchant merchant = requireMerchant(request.getMerchantId());

        merchant.setLegalName(request.getLegalName().trim());
        merchant.setDisplayName(request.getDisplayName().trim());
        merchant.setUpdatedAt(Instant.now());

        return merchantRepository.save(merchant);
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
        validateNotNull(request, "request");
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

        return merchantRepository.save(merchant);
    }

    // ---------------------------------------------------------------------------
    // Queries
    // ---------------------------------------------------------------------------

    /**
     * Returns a single merchant by its id.
     * Throws if no merchant with that id exists.
     */
    public Merchant getMerchantById(UUID merchantId) {
        validateNotNull(merchantId, "merchantId");
        return requireMerchant(merchantId);
    }

    /**
     * Returns a list of merchants matching the search text.
     * Matches against legalName and displayName using case-insensitive contains.
     * If searchText is blank or null, all merchants are returned.
     */
    public List<Merchant> searchMerchants(SearchMerchantsRequest request) {
        String text = (request == null || request.getSearchText() == null)
                ? ""
                : request.getSearchText().trim();

        List<Merchant> results = new ArrayList<>();
        for (Merchant merchant : merchantRepository.findAll()) {
            if (text.isEmpty() || matchesSearchText(merchant, text)) {
                results.add(merchant);
            }
        }
        return results;
    }

    /**
     * Returns true if the merchant's legalName or displayName contains
     * the search text (case-insensitive).
     */
    private boolean matchesSearchText(Merchant merchant, String text) {
        String lower = text.toLowerCase();
        boolean legalNameMatches = merchant.getLegalName() != null
                && merchant.getLegalName().toLowerCase().contains(lower);
        boolean displayNameMatches = merchant.getDisplayName() != null
                && merchant.getDisplayName().toLowerCase().contains(lower);
        return legalNameMatches || displayNameMatches;
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
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found: " + merchantId));
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
