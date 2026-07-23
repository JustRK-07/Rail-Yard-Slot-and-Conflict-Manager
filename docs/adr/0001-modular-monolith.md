# ADR 0001: Use a modular monolith

- **Status:** Accepted and implemented for the foundation and master-data slices.
- **Date:** 2026-07-22

## Context

The project needs clear business boundaries, relational transactions, integration tests, and a reproducible portfolio deployment. Separate services would add network contracts, distributed tracing, and cross-service consistency before the core scheduling rules exist.

## Decision

Use one Spring Boot application organized by business feature. Angular and PostgreSQL remain separate runtime processes.

## Consequences

- Reservation and audit changes can share one transaction.
- Local development and CI remain understandable.
- Module boundaries must be maintained through package ownership rather than network isolation.
- A module can be extracted later only if measured scale or team ownership justifies it.

## Implementation status

The `yard`, `track`, and `train` modules are wired end-to-end (entity, repository, DTOs, service, controller, integration tests). The `reservation`, `recommendation`, and `audit` modules currently contain only `package-info.java` placeholders and are scheduled for the next slice.
