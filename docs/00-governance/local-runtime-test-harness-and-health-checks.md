# Local Runtime, Test Harness, and Health Checks

This document locks the foundation runtime and smoke-check path for Meridian.

## Purpose

Meridian should be easy to boot locally, easy to verify with one stable test command, and easy to smoke-check through a predictable health endpoint.

## Standard commands

### Run tests

mvn -q clean verify

### Run locally with local profile

mvn spring-boot:run -Dspring-boot.run.profiles=local

## Health endpoint

The local runtime health endpoint is:

/health

Expected response shape:

{
  "status": "UP",
  "application": "meridian"
}

## Rules

1. Local verification must work with one stable command path.
2. Tests must run using the test profile.
3. Local runtime must run using the local profile.
4. Health checks should stay simple and predictable in the foundation phase.
5. Future feature work should not break the base smoke-check path.

## Why this matters

This gives Meridian one reliable way to prove the application starts, tests cleanly, and responds to a predictable health check before business features are added.
