# Merchant Core Lifecycle Rules

## Purpose
Merchant Core owns the high-level merchant lifecycle profile.
It does not own onboarding workflow or product enablement workflow.

## Statuses
- PENDING
- ACTIVE
- SUSPENDED
- CLOSED

## Allowed Transitions
- PENDING -> ACTIVE
- ACTIVE -> SUSPENDED
- SUSPENDED -> ACTIVE
- ACTIVE -> CLOSED
- SUSPENDED -> CLOSED

## Forbidden Transitions
- CLOSED -> ACTIVE
- CLOSED -> SUSPENDED
- PENDING -> CLOSED
- Any transition not listed as allowed

## Status History Expectation
Every merchant status change should later record:
- previous status
- new status
- changed by
- changed at
- reason when required