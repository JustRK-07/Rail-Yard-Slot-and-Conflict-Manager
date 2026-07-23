# ADR 0001: Use a modular monolith

- **Status:** Accepted
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
