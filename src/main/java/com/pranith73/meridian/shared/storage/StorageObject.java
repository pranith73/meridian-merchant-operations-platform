package com.pranith73.meridian.shared.storage;

/**
 * Represents a file object to be stored or retrieved.
 *
 * This is a shared transport model between business code
 * and a future storage implementation.
 */
public record StorageObject(
        String storageKey,
        String fileName,
        String contentType,
        byte[] content
) {
}