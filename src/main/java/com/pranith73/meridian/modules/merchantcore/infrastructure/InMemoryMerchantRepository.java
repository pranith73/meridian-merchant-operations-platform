package com.pranith73.meridian.modules.merchantcore.infrastructure;

import com.pranith73.meridian.modules.merchantcore.application.MerchantRepository;
import com.pranith73.meridian.modules.merchantcore.domain.Merchant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Temporary in-memory implementation of MerchantRepository.
 *
 * Merchants are kept in a plain HashMap for the duration of the application's
 * lifetime. This lets the application service and its business rules be
 * developed and tested before a real database is wired in.
 *
 * When real database persistence is ready, create a new class (e.g.
 * JdbcMerchantRepository) that implements MerchantRepository and replace
 * this class wherever it is wired up. Nothing else needs to change.
 */
public class InMemoryMerchantRepository implements MerchantRepository {

    // Key: merchantId, Value: the Merchant object.
    private final Map<UUID, Merchant> store = new HashMap<>();

    /**
     * Stores the merchant using its merchantId as the key.
     * Overwrites an existing entry if the id is already present (update).
     */
    @Override
    public Merchant save(Merchant merchant) {
        store.put(merchant.getMerchantId(), merchant);
        return merchant;
    }

    /**
     * Returns the merchant with the given id, or Optional.empty() if not found.
     */
    @Override
    public Optional<Merchant> findById(UUID merchantId) {
        return Optional.ofNullable(store.get(merchantId));
    }

    /**
     * Returns all merchants currently in the store.
     * A new list is returned so the caller cannot modify the internal map.
     */
    @Override
    public List<Merchant> findAll() {
        return new ArrayList<>(store.values());
    }
}
