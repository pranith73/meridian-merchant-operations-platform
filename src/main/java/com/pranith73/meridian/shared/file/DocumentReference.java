package com.pranith73.meridian.shared.file;

import java.time.Instant;

/**
 * Shared metadata reference for a stored document.
 *
 * This is not the file content itself.
 * It represents the governed reference to a file.
 */
public record DocumentReference(
        String documentId,
        String fileName,
        String contentType,
        long sizeInBytes,
        String storageKey,
        DocumentStatus status,
        Instant createdAt
) {
}