package com.pranith73.meridian.shared.storage;

import com.pranith73.meridian.shared.file.DocumentAccessGrant;

/**
 * Shared abstraction for file storage behavior.
 *
 * Real implementations may later store files in local disk,
 * object storage, or another managed storage system.
 */
public interface StorageGateway {

    void store(StorageObject storageObject);

    StorageObject load(String storageKey);

    DocumentAccessGrant createAccessGrant(String documentId, String storageKey);
}