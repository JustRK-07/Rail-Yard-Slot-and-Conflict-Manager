# Reservation concurrency

## Interval semantics

Reservations use half-open ranges:

```text
[occupied_from, occupied_until)
```

Two windows conflict when:

```text
existing.start < requested.end
AND existing.end > requested.start
```

Consequences:

- `09:00–10:00` and `10:00–11:00` are adjacent and allowed.
- `09:00–10:00` and `09:59–11:00` overlap and are rejected.
- Track setup and clearance buffers expand `occupied_from` and `occupied_until` before this comparison.

## Why an application check is not enough

Two requests can query availability at the same time, both observe no conflict, and then both attempt to insert. A Java `if` statement or repository lookup cannot close that race by itself.

PostgreSQL therefore enforces the invariant with a partial GiST exclusion constraint:

```sql
EXCLUDE USING gist (
    track_id WITH =,
    tstzrange(occupied_from, occupied_until, '[)') WITH &&
)
WHERE (status IN ('PLANNED', 'ACTIVE'));
```

`btree_gist` supplies equality support for UUID track IDs. The partial predicate means completed and cancelled reservations do not block future use.

## Planned transaction flow

1. Validate yard, train, track, capability, status, and requested times.
2. Calculate the buffered effective window.
3. Query current conflicts to return useful details early.
4. Insert the reservation and audit event in one Spring transaction.
5. Let PostgreSQL perform the final overlap check.
6. Translate exclusion SQLSTATE `23P01` into a stable `409 TRACK_RESERVATION_CONFLICT` response.
7. Roll back both reservation and audit data on any failure.

The database constraint remains authoritative even if another writer bypasses the recommendation endpoint.

## Verification

`DatabaseMigrationTests` starts PostgreSQL 17 through Testcontainers and verifies that adjacent ranges commit while an overlapping range raises `DataIntegrityViolationException`. A later API integration test will send concurrent requests and assert one successful reservation and one conflict response.
