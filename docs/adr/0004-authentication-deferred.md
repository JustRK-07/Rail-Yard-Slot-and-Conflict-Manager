# ADR 0004: Defer authentication

- **Status:** Accepted for MVP
- **Date:** 2026-07-22

## Context

The internship-aligned evidence is scheduling logic, SQL design, tests, Angular integration, and delivery workflow. Building an identity system first would delay those differentiating features.

## Decision

Run the MVP as a single local demo operator and store a documented actor label in audit records. Do not store passwords or pretend the demo has production authorization.

## Consequences

- Scheduling work can be delivered and tested first.
- API endpoints are not suitable for public deployment until authentication and authorization are added.
- Audit actor fields remain compatible with a later identity provider.
- Security documentation must clearly state the limitation.
