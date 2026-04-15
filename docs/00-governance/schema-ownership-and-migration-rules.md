# Schema Ownership and Migration Rules

This document locks schema ownership and migration discipline for Meridian Foundation Task T-005.

## Purpose

Before persistence work begins, Meridian must make database ownership explicit.
This prevents cross-module drift, random migration placement, and hidden schema coupling.

## Core rule

Each module owns its own schema area and its own migration path.
No module may add, change, or delete another module's canonical persistence structures casually.

## Schema ownership map

- merchantcore -> merchant_core
- onboarding -> onboarding
- productenablement -> product_enablement
- transactioninquiry -> transaction_inquiry
- statementsreporting -> statements_reporting
- chargebacks -> chargebacks
- pciassistance -> pci_assistance
- supportops -> support_ops
- securityaudit -> security_audit
- integrationedge -> integration_edge

## Migration root

All migrations live under:

src/main/resources/db/migration

## Module migration paths

- src/main/resources/db/migration/merchantcore
- src/main/resources/db/migration/onboarding
- src/main/resources/db/migration/productenablement
- src/main/resources/db/migration/transactioninquiry
- src/main/resources/db/migration/statementsreporting
- src/main/resources/db/migration/chargebacks
- src/main/resources/db/migration/pciassistance
- src/main/resources/db/migration/supportops
- src/main/resources/db/migration/securityaudit
- src/main/resources/db/migration/integrationedge

## Naming rules

### Schema names
Use lowercase snake_case schema names.

Examples:
- merchant_core
- product_enablement
- security_audit

### Future table names
Use lowercase snake_case table names.

Examples:
- merchant_profile
- onboarding_application
- chargeback_case

### Future migration file names
Keep migration names explicit and module-owned.

Example style:
- V001__merchantcore_create_initial_schema.sql
- V002__merchantcore_create_merchant_tables.sql

## Ownership rules

1. A module owns its own canonical persistence structures.
2. Another module must not write migrations into that module's folder.
3. Cross-module references must be deliberate and reviewed.
4. Shared convenience tables must not become a backdoor around ownership.

## Rollback assumptions

1. Migrations should be forward-safe first.
2. Rollback must be considered before applying destructive changes.
3. Drops, renames, and destructive data changes require extra care and explicit review.
4. Early Meridian development should prefer additive migrations over destructive rewrites.

## Why this matters

This keeps persistence aligned with the frozen modular-monolith boundaries and makes later schema work safer, more reviewable, and easier to explain.
