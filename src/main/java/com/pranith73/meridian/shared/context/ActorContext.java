package com.pranith73.meridian.shared.context;

/**
 * Minimal actor placeholder for the current request.
 * This is only a foundation shell.
 * It does not implement real authentication yet.
 */
public record ActorContext(
        String actorId,
        String actorType
) {
}