# ADR 0002: Use UTC half-open time windows

- **Status:** Accepted and enforced by the schema in the foundation slice.
- **Date:** 2026-07-22

## Context

Rail yards can operate in different time zones, and schedule boundaries must have one unambiguous overlap rule.

## Decision

Persist timestamps as UTC `TIMESTAMPTZ`, represent them as Java `Instant`, and display them in the yard's configured IANA time zone. Treat reservation windows as half-open `[start, end)` ranges.

## Consequences

- Adjacent reservations can share a boundary without overlapping.
- Daylight-saving display behavior is delegated to named time-zone rules rather than fixed offsets.
- Setup and clearance buffers must be applied before storing the effective occupancy range.
- Unit tests must cover boundary equality and buffer-created conflicts.

## Implementation status

`V2__create_core_schema.sql` declares `track_reservations.occupied_from` and `track_reservations.occupied_until` as `TIMESTAMPTZ`, and `V3__prevent_overlapping_reservations.sql` enforces half-open semantics through `tstzrange(occupied_from, occupied_until, '[)')`. `DatabaseMigrationTests.permitsAdjacentWindowsAndRejectsAnOverlapOnTheSameTrack` proves the boundary semantics.
