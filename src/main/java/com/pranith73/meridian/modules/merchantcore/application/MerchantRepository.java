package com.pranith73.meridian.modules.merchantcore.application;

import com.pranith73.meridian.modules.merchantcore.domain.Merchant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines how Merchant Core stores and retrieves merchants.
 *
 * This is an interface — it describes what operations are needed without
 * specifying how they are implemented. The application service depends on
 * this interface, not on any specific storage technology. When we are ready
 * to switch from in-memory storage to a real database, only the implementation
 * changes; nothing in the application service needs to be touched.
 */
public interface MerchantRepository {

    /**
     * Saves a merchant. If the merchant already exists it is updated;
     * if it is new it is inserted. Returns the saved merchant.
     */
    Merchant save(Merchant merchant);

    /**
     * Finds a merchant by its id.
     * Returns Optional.empty() if no merchant with that id exists.
     */
    Optional<Merchant> findById(UUID merchantId);

    /**
     * Returns every merchant in the store.
     * Used by search to filter in memory until a real query layer exists.
     */
    List<Merchant> findAll();
}
