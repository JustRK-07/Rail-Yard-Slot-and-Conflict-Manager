# Demo script

## Foundation checkpoint

The current development slice demonstrates engineering setup rather than the final dispatcher workflow.

1. Run the backend Testcontainers tests.
2. Show Flyway applying three migrations to an empty PostgreSQL 17 database.
3. Show that adjacent reservations are accepted.
4. Show that an overlapping reservation is rejected by the GiST exclusion constraint.
5. Run Angular unit tests and the production build.
6. Start the Compose stack and open the operations shell.
7. Navigate through Occupancy, Assign a train, Trains, Tracks, and Reservations.
8. Explain that each route is intentionally marked as planned until its API workflow is implemented.

## Target MVP demonstration

The completed MVP will use yard `YD-A`, tracks `T01`–`T05`, and synthetic freight trains:

1. Open yard occupancy.
2. Request a track for `FR-101` from 09:30 to 11:00 UTC.
3. Show `T01` excluded because it is too short.
4. Show `T02` excluded because its buffered reservation overlaps.
5. Show `T03` excluded because it is under maintenance.
6. Compare the ranked valid alternatives.
7. Reserve the top recommendation.
8. Attempt another overlapping reservation and show the `409` response.
9. Cancel or reschedule the reservation.
10. Review its audit history and run the full test suite.

Only implemented behavior should be presented as complete.
