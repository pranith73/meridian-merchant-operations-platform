package com.pranith73.meridian.shared.av;

import java.time.Instant;

/**
 * Result of scanning a stored file.
 */
public record ScanResult(
        String storageKey,
        ScanStatus status,
        String detail,
        Instant scannedAt
) {
}