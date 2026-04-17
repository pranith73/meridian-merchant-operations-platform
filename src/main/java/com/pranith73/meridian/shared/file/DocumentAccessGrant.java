package com.pranith73.meridian.shared.file;

import java.time.Instant;

/**
 * Represents a controlled access grant for retrieving a document.
 *
 * This models the idea of signed or time-limited access
 * without implementing real storage logic yet.
 */
public record DocumentAccessGrant(
        String documentId,
        String accessToken,
        Instant expiresAt
) {
}