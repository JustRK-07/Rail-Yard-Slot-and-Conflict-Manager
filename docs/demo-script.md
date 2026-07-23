# Demo script

## Foundation and master-data checkpoint (current slice)

The committed code demonstrates engineering setup, the database, and the master-data APIs. Each step has been verified this session.

1. Build the backend in a Java 21 Docker container and run the full test suite.
   ```bash
   docker run --rm --network host -v /var/run/docker.sock:/var/run/docker.sock \
     -v "$PWD/backend:/workspace" -v rail-yard-maven-cache:/root/.m2 \
     -w /workspace maven:3.9-eclipse-temurin-21 ./mvnw verify
   ```
2. Show Flyway applying three migrations to an empty PostgreSQL 17 database.
3. Show that adjacent reservations are accepted and that an overlapping reservation on the same track is rejected by the GiST exclusion constraint.
4. Run Angular unit tests and the production build.
   ```bash
   npm --prefix frontend test -- --watch=false
   npm --prefix frontend run build
   ```
5. Start the Compose stack and open the operations shell.
   ```bash
   POSTGRES_PORT=55432 docker compose up --build
   ```
6. Load the deterministic demo data so the synthetic yard, tracks, and trains are visible.
   ```bash
   docker compose exec -T db psql -U rail_yard -d rail_yard < scripts/demo-data.sql
   ```
7. Exercise the master-data APIs with `curl` to confirm the stable error envelope, correlation IDs, and pagination.
   ```bash
   curl -s -X POST http://127.0.0.1:8080/api/yards -H 'Content-Type: application/json' \
     -d '{"code":"APX-1","name":"Alpha Yard","location":"Pune","timeZone":"Asia/Kolkata"}'
   curl -s -X POST http://127.0.0.1:8080/api/yards -H 'Content-Type: application/json' \
     -d '{"code":"APX-1","name":"Alpha Yard","location":"Pune","timeZone":"Asia/Kolkata"}'
   ```
   The first request returns 201; the second returns 409 with `code: DUPLICATE_RESOURCE` and a non-empty `correlationId`.
8. Open the OpenAPI document at `http://localhost:8080/v3/api-docs` and the Swagger UI at `http://localhost:8080/swagger-ui/index.html`.
9. Browse the operations shell at `http://localhost:4200/occupancy` and explain that each route is intentionally marked as planned until its API workflow is implemented.

## Target MVP demonstration (not yet implemented)

When the scheduling and Angular workflow slices are complete, the same stack will demonstrate:

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

Only implemented behavior should be presented as complete. Until the scheduling slice ships, steps 1–10 above are forward-looking and must not be claimed as live.
