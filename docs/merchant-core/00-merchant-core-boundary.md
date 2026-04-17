# Merchant Core Boundary and Non-Goals

## Purpose
Merchant Core is the single trusted place for merchant identity and merchant details in Meridian.

## Owns
- merchant profile
- merchant contacts
- merchant locations
- merchant account / MID references
- settlement profile context
- high-level merchant lifecycle profile

## Does Not Own
- onboarding workflow state
- product enablement / entitlements
- support ticket truth
- chargeback case truth
- PCI workflow truth
- processor payment or settlement truth

## Allowed Usage
- other modules may use merchantId as reference
- other modules may read Merchant Core data through proper queries
- other modules must not directly change Merchant Core truth

## Drift Warnings
Red flags:
- onboarding status added here
- product enabled flags added here
- support queue fields added here
- chargeback fields added here
- PCI workflow fields added here

## Simple Meaning
Merchant Core tells Meridian who the merchant is.
It does not own every workflow related to the merchant.