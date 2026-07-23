# ADR 0004: Defer authentication

- **Status:** Accepted for MVP; remains in effect for the master-data slice.
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

## Implementation status

The current master-data APIs do not require authentication. The `track_reservations` and `audit_events` tables include `created_by`, `updated_by`, and `actor_id` columns so audit events can carry a documented identity once authentication is added.
