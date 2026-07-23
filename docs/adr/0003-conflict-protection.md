# ADR 0003: Enforce overlaps in PostgreSQL

- **Status:** Accepted and implemented in `V3__prevent_overlapping_reservations.sql`.
- **Date:** 2026-07-22

## Context

An application-level availability query has a race between checking a track and inserting a reservation. Concurrent dispatchers must not commit overlapping blocking reservations.

## Decision

Use an indexed pre-check for useful conflict details and a partial PostgreSQL GiST exclusion constraint as the authoritative invariant. The constraint compares `yard_id` and `track_id` equality with the overlap of half-open `tstzrange(occupied_from, occupied_until, '[)')` values for `PLANNED` and `ACTIVE` reservations.

## Consequences

- Concurrent writers cannot both commit an overlap.
- Tests must run against PostgreSQL rather than an in-memory substitute.
- SQLSTATE `23P01` must be translated to the public `409` API contract.
- Database-specific behavior is accepted because it directly demonstrates the RDBMS requirement.
- The constraint key intentionally includes `yard_id` so a future migration that drops the `(track_id, yard_id)` composite FK cannot silently re-introduce a cross-yard hazard.

## Implementation status

- `V3__prevent_overlapping_reservations.sql` creates `ex_reservations_no_track_overlap` with `yard_id WITH =`, `track_id WITH =`, and `tstzrange(...) WITH &&`, partial on `status IN ('PLANNED', 'ACTIVE')`.
- `V2__create_core_schema.sql` adds `ck_reservations_buffer_bounds` to bound buffers at 24 hours so a compromised writer cannot bypass the GiST constraint.
- `DatabaseMigrationTests` verifies that adjacent ranges commit and overlapping ranges raise `DataIntegrityViolationException`.
- The reservation service that translates `23P01` to `409 TRACK_RESERVATION_CONFLICT` is planned for the next slice.
