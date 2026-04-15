# File, Storage, and AV Boundaries

This document locks file-handling safety boundaries for the Meridian foundation.

## Purpose

Meridian must have one safe document-handling path before business modules start using files.

## Core rules

1. Files begin in quarantine.
2. A file must not become usable before scanning is complete.
3. File retrieval must go through controlled access.
4. Business modules must not bypass storage or antivirus abstractions.
5. File metadata and file content are separate concerns.

## Shared boundaries

### File boundary
Shared file models represent document identity, status, and access posture.

### Storage boundary
Storage behavior must be accessed only through the shared storage gateway.

### Antivirus boundary
Scanning behavior must be accessed only through the shared antivirus gateway.

## Allowed status flow

- QUARANTINED
- SCAN_PENDING
- AVAILABLE
- REJECTED

## Why this matters

This prevents unsafe direct file handling and keeps later onboarding, reporting, chargebacks, and PCI flows aligned to one governed approach.
