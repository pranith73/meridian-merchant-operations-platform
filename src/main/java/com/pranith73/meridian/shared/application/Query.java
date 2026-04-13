package com.pranith73.meridian.shared.application;

/**
 * Marker interface for all query objects in the application layer.
 *
 * Why this exists:
 * - A query represents a read request.
 * - It helps us separate read use cases from write use cases.
 * - Later modules can create query classes that implement this interface.
 *
 * Example future usage:
 * - GetMerchantByIdQuery
 * - SearchApplicationsQuery
 */
public interface Query<R> {
    // Marker interface only.
    // No methods are needed right now.
}