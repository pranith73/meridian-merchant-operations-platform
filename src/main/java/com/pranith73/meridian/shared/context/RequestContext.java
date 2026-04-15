package com.pranith73.meridian.shared.context;

/**
 * Request-scoped context used to carry traceability information.
 */
public record RequestContext(
        String correlationId,
        ActorContext actor
) {
}