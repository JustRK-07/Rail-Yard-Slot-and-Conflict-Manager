# Development roadmap

This checklist is the project-level source of truth for delivery progress. A checked item means the implementation exists and has been verified locally; CI-only items remain open until a GitHub Actions run confirms them.

## Milestone 0 — Foundation

### Repository and architecture

- [x] Initialize the local `main` repository and configure the SSH origin.
- [x] Add repository hygiene, environment example, architecture, ER, concurrency, and ADR documentation.
- [x] Document synthetic data and the lack of Wabtec/rail-operator affiliation.
- [ ] Push the repository after GitHub SSH access is configured.

### Backend

- [x] Scaffold Java 21 and Spring Boot 4.0.7 with Maven Wrapper.
- [x] Configure PostgreSQL, Flyway, JPA validation, UTC handling, Actuator, and OpenAPI.
- [x] Create feature packages for yard, track, train, reservation, recommendation, and audit.
- [x] Run the Spring context test against PostgreSQL 17 with Testcontainers.

### Frontend

- [x] Scaffold Angular 21 with strict TypeScript, standalone components, routing, SCSS, and Vitest.
- [x] Build the accessible operations shell and five feature routes.
- [x] Add unit tests for the shell and route-driven module view.
- [x] Pass the production Angular build without budget warnings.

### Database

- [x] Add normalized tables for yards, tracks, track capabilities, trains, train requirements, reservations, and audit events.
- [x] Add foreign keys, business checks, unique keys, optimistic versions, and query indexes.
- [x] Add the partial GiST exclusion constraint for blocking reservation overlap.
- [x] Verify adjacent and overlapping ranges against PostgreSQL 17.
- [x] Add deterministic synthetic demo SQL.

### Delivery infrastructure

- [x] Add PostgreSQL, backend, and frontend services to Docker Compose.
- [x] Validate the Compose configuration.
- [x] Build both application images locally.
- [x] Add backend and frontend multi-stage Dockerfiles.
- [x] Add GitHub Actions jobs for backend, frontend, production audit, and container builds.
- [ ] Confirm the first GitHub Actions run after the repository is pushed.

## Milestone 1 — Master data APIs

- [x] Implement `Yard` entity, repository, DTOs, service, and CRUD endpoints.
- [x] Implement `Track` and `TrackCapability` persistence and endpoints.
- [x] Implement `Train` and required-capability persistence and endpoints.
- [x] Add pagination, stable sorting, validation, and not-found handling.
- [x] Add the shared RFC-style API error contract and correlation IDs.
- [x] Add repository and API integration tests.
- [ ] Connect Angular train and track read views.

## Milestone 2 — Scheduling and reservations

- [ ] Implement `TimeWindow` and half-open overlap unit tests.
- [ ] Implement track compatibility filtering.
- [ ] Implement deterministic `PriorityQueue` recommendation ranking.
- [ ] Return included and excluded tracks with stable reason codes.
- [ ] Implement `POST /api/scheduling/recommendations`.
- [ ] Implement transactional reservation creation and atomic audit insertion.
- [ ] Translate PostgreSQL SQLSTATE `23P01` into `409 TRACK_RESERVATION_CONFLICT`.
- [ ] Implement reschedule, activate, complete, and cancel transitions.
- [ ] Add concurrent-request integration tests proving one winner.

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

## Definition of done

A task is complete only when its acceptance behavior is implemented, relevant tests pass, database changes are versioned, documentation is updated, and no credentials or generated output are tracked.
