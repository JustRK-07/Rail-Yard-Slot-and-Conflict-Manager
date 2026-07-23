# ADR 0003: Enforce overlaps in PostgreSQL

- **Status:** Accepted
- **Date:** 2026-07-22

## Context

An application-level availability query has a race between checking a track and inserting a reservation. Concurrent dispatchers must not commit overlapping blocking reservations.

## Decision

Use an indexed pre-check for useful conflict details and a partial PostgreSQL GiST exclusion constraint as the authoritative invariant. The constraint compares UUID track equality and overlap of half-open `tstzrange` values for `PLANNED` and `ACTIVE` reservations.

## Consequences

- Concurrent writers cannot both commit an overlap.
- Tests must run against PostgreSQL rather than an in-memory substitute.
- SQLSTATE `23P01` must be translated to the public `409` API contract.
- Database-specific behavior is accepted because it directly demonstrates the RDBMS requirement.
