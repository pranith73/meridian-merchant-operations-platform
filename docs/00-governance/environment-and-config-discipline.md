# Environment and Config Discipline

This document is the single reference for Meridian runtime configuration locations in the Foundation phase.

## Purpose

T-004 requires environment and config discipline so runtime behavior stays predictable across local, dev, and test.

## Config locations

- `src/main/resources/application.properties`
    - base shared defaults only
- `src/main/resources/application-local.properties`
    - local developer overrides
- `src/main/resources/application-dev.properties`
    - shared development environment overrides
- `src/main/resources/application-test.properties`
    - automated test profile

## Rules

1. Do not store secrets in committed source files.
2. Secrets must come from environment variables or external runtime config.
3. `application.properties` stays environment-neutral.
4. Local-only behavior belongs in `application-local.properties`.
5. Test behavior must use the `test` profile explicitly.
6. Future environment-specific settings should follow this same structure instead of being mixed into one file.

## Current profile usage

- Local manual run:
    - `local`
- Shared dev run:
    - `dev`
- Automated tests:
    - `test`

## Why this matters

This keeps startup behavior predictable, makes tests reproducible, and prevents accidental dependency on one developer machine's settings.