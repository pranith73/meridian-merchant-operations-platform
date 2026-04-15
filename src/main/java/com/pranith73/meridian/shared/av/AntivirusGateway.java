package com.pranith73.meridian.shared.av;

/**
 * Shared abstraction for antivirus scanning.
 *
 * Real implementations may later call an external AV engine
 * or internal scanning service.
 */
public interface AntivirusGateway {

    ScanResult scan(String storageKey);
}