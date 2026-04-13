package com.pranith73.meridian.shared.application;

/**
 * Contract for handling a specific query type.
 *
 * Why this exists:
 * - It gives us a standard pattern for read use cases.
 * - Later, each module can define its own query handlers in a consistent way.
 * - It keeps reads explicit instead of scattering lookup logic everywhere.
 *
 * Example future usage:
 * - GetMerchantByIdQueryHandler implements QueryHandler<GetMerchantByIdQuery, MerchantView>
 */
public interface QueryHandler<Q extends Query<R>, R> {

    /**
     * Handles the incoming query and returns the result.
     *
     * @param query the query object containing read criteria
     * @return the read result for that query
     */
    R handle(Q query);
}