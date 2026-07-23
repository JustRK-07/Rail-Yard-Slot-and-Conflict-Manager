# Development roadmap

This checklist is the project-level source of truth for delivery progress. A checked item means the implementation exists and has been verified locally. Open items remain open until they are implemented and verified.

## Current status snapshot

- **Milestone 0 — Foundation:** all items complete locally; the first GitHub Actions run is pending the initial push.
- **Milestone 1 — Master-data APIs:** all backend items complete; Angular read views for train and track are pending.
- **Milestone 2 — Scheduling and reservations:** not started.
- **Milestone 3 — Dispatcher workflow:** not started.
- **Milestone 4 — Portfolio delivery:** not started.
- **Local commit history:** `15afa4e` (docs) on top of `9f9381b` (frontend), `0470465` (backend), `f756aa2` (metadata).
- **Repository remote:** `git@github.com:JustRK-07/Rail-Yard-Slot-and-Conflict-Manager.git`. SSH on this host returns `Permission denied (publickey)`, so the push is deferred until GitHub SSH access is configured.

## Milestone 0 — Foundation

### Repository and architecture

- [x] Initialize the local `main` repository and configure the SSH origin.
- [x] Add repository hygiene, environment example, architecture, ER, concurrency, and ADR documentation.
- [x] Document synthetic data and the lack of affiliation with any railway operator or equipment vendor.
- [ ] Push the repository after GitHub SSH access is configured.

### Backend

- [x] Scaffold Java 21 and Spring Boot 4.0.7 with Maven Wrapper.
- [x] Configure PostgreSQL, Flyway, JPA validation, UTC handling, Actuator, and OpenAPI.
- [x] Create feature packages for yard, track, train, common, reservation, recommendation, and audit.
- [x] Run the Spring context test against PostgreSQL 17 with Testcontainers.
- [x] Define the shared RFC-style API error envelope, correlation ID filter, and stable page serialization.

### Frontend

- [x] Scaffold Angular 21 with strict TypeScript, standalone components, routing, SCSS, and Vitest.
- [x] Build the accessible operations shell and five feature routes.
- [x] Add unit tests for the shell and route-driven module view.
- [x] Pass the production Angular build without budget warnings.

### Database

- [x] Add normalized tables for yards, tracks, track capabilities, trains, train requirements, reservations, and audit events.
- [x] Add foreign keys, business checks, unique keys, optimistic versions, and query indexes.
- [x] Add the partial GiST exclusion constraint (yard_id + track_id + tstzrange overlap) for blocking reservation overlap.
- [x] Add the 24-hour buffer-bound CHECK constraint to prevent infinite occupied_until values.
- [x] Verify adjacent and overlapping ranges against PostgreSQL 17.
- [x] Add deterministic synthetic demo SQL.

### Delivery infrastructure

- [x] Add PostgreSQL, backend, and frontend services to Docker Compose.
- [x] Validate the Compose configuration.
- [x] Build both application images locally.
- [x] Add backend and frontend multi-stage Dockerfiles.
- [x] Add GitHub Actions jobs for backend, frontend, production audit, and container builds.
- [ ] Confirm the first GitHub Actions run after the repository is pushed.

## Milestone 1 — Master-data APIs

- [x] Implement `Yard` entity, repository, DTOs, service, and CRUD endpoints.
- [x] Implement `Track` and `TrackCapability` persistence and endpoints, including the PATCH-status endpoint.
- [x] Implement `Train` and required-capability persistence and endpoints, including the case-insensitive search query.
- [x] Add pagination, stable sorting, validation, and not-found handling.
- [x] Add the shared RFC-style API error contract and correlation IDs.
- [x] Add repository and API integration tests.
- [ ] Connect Angular train and track read views to the new backend APIs.

## Milestone 2 — Scheduling and reservations

- [ ] Implement `TimeWindow` and half-open overlap unit tests.
- [ ] Implement track compatibility filtering (purpose, capability, length, status, occupancy).
- [ ] Implement deterministic `PriorityQueue` recommendation ranking with stable tie-breakers.
- [ ] Return included and excluded tracks with stable reason codes and human-readable explanations.
- [ ] Implement `POST /api/scheduling/recommendations`.
- [ ] Implement transactional reservation creation with atomic audit-event insertion.
- [ ] Translate PostgreSQL SQLSTATE `23P01` into `409 TRACK_RESERVATION_CONFLICT`.
- [ ] Implement reschedule, activate, complete, and cancel transitions.
- [ ] Add concurrent-request integration tests proving one winner and one conflict.

## Milestone 3 — Dispatcher workflow

- [ ] Build the typed Angular API layer and centralized error handling.
- [ ] Build the train-assignment reactive form.
- [ ] Render ranked recommendations and exclusion explanations.
- [ ] Preserve form state and refresh alternatives after a `409` response.
- [ ] Build reservation detail and audit-history pages.
- [ ] Build the accessible yard occupancy timeline.
- [ ] Add loading, empty, error, success, and conflict states.
- [ ] Add component, service, and end-to-end workflow tests.

## Milestone 4 — Portfolio delivery

- [ ] Add complete OpenAPI examples and endpoint documentation.
- [ ] Run the deterministic browser demo from a clean clone.
- [ ] Record measured backend and frontend test coverage.
- [ ] Add real screenshots after the workflows exist.
- [ ] Run security and dependency reviews.
- [ ] Measure before documenting any latency or performance claim.
- [ ] Select and complete an AWS deployment only after the local MVP is stable.
- [ ] Replace resume templates only with verified implementation facts.
- [ ] Choose and commit a LICENSE file.

## Definition of done

A task is complete only when its acceptance behavior is implemented, relevant tests pass, database changes are versioned, documentation is updated, and no credentials or generated output are tracked.
