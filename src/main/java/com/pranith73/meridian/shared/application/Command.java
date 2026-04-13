package com.pranith73.meridian.shared.application;

/**
 * Marker interface for application-layer commands.
 *
 * <p>A command represents an intentional action that asks the system
 * to change something or perform a use case.</p>
 *
 * <p>The generic type R represents the result returned after handling
 * the command.</p>
 *
 * <p>Examples later in real modules might be:
 * - CreateMerchantCommand
 * - ApproveApplicationCommand
 * - ActivateEnrollmentCommand
 * </p>
 */
public interface Command<R> {
}