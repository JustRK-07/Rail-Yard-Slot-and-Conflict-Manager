# Project Overview — Rail Yard Slot and Conflict Manager

> This document describes what the repository actually contains as of the latest commit on `main`. It is the most accurate single-source description of the code that is committed and the code that is still pending. It is the right thing to read before reading `PROJECT_SPEC.md`, which describes the target product.
>
> **Last refreshed against commit `15afa4e docs: add demo data script and project overview for the implemented slice`.**
> The repository currently implements Milestone 0 (foundation) and Milestone 1 (master-data APIs). Milestones 2–4 (scheduling, dispatcher workflow, portfolio delivery) are still pending and are tracked in [`docs/ROADMAP.md`](docs/ROADMAP.md).

## 1. What this project is

A full-stack portfolio implementation of a rail-yard scheduling system built to demonstrate the Wabtec Computer Science Engineering internship competencies:

- A working backend REST API in Java 21 and Spring Boot 4.0.
- A working Angular 21 dispatcher operations shell.
- A normalized relational schema in PostgreSQL 17 with versioned Flyway migrations, integrity checks, a partial GiST exclusion constraint that enforces non-overlapping track reservations, and an upper bound on buffer windows.
- A reproducible Docker Compose stack for local development.
- A GitHub Actions CI pipeline for backend, frontend, and container builds.
- An RFC-style error contract, correlation IDs, structured logging, and stable pagination across the API.
- Testcontainers-backed integration tests against real PostgreSQL instead of an in-memory database.

The current state of the repository implements **Milestone 0 (foundation)** and **Milestone 1 (master-data APIs)** of the five-milestone roadmap. The scheduling workflow, Angular feature wiring, and occupancy timeline are still planned.

## 2. Repository layout

```text
rail-yard-slot-conflict-manager/
├── backend/                  Java 21 + Spring Boot 4.0 modular monolith
├── frontend/                 Angular 21 dispatcher operations shell
├── docs/                     Architecture, ER, concurrency, demo, ADRs, roadmap
├── scripts/                  Deterministic synthetic demo SQL
├── compose.yaml              Local PostgreSQL + backend + frontend
├── PROJECT_SPEC.md           Target product specification (30 sections)
├── PROJECT_OVERVIEW.md       This document
├── README.md                 Quickstart and project status
├── .editorconfig
├── .env.example
├── .gitignore
├── LICENSE                   (not yet chosen; see Roadmap)
└── .github/workflows/ci.yml
```

The repository is on `main` with four local commits. The remote is configured as `git@github.com:JustRK-07/Rail-Yard-Slot-and-Conflict-Manager.git`. SSH on this host currently returns `Permission denied (publickey)`, so the push is deferred until GitHub SSH access is configured. `git status` is clean.

## 3. Backend — what is implemented

### 3.1 Stack and configuration

- **Java 21** source/target. Built and tested through `maven:3.9-eclipse-temurin-21` in a Docker container because the host only has JDK 17.
- **Spring Boot 4.0.7** parent POM.
- **Spring Data JPA**, **Spring Web MVC**, **Spring Validation**, **Spring Flyway**, **Spring Actuator**, **springdoc OpenAPI starter (3.0.2)**.
- **PostgreSQL JDBC** runtime driver; **Flyway database PostgreSQL** module; **Testcontainers PostgreSQL**, **JUnit Jupiter**, **spring-boot-starter-webmvc-test** for tests.
- **PostgreSQL 17-alpine** for the local stack and for Testcontainers.
- `application.yml` uses the Spring Boot 4.0 property namespaces (`spring.web.error.*`, `management.health.probes.enabled`). Server error responses never leak binding errors, exceptions, messages, or stack traces. UTC is enforced on JDBC, Jackson, and the database.

### 3.2 Package layout

The package root is `com.justrk07.railyard`. Code is organised by business feature:

```text
com.justrk07.railyard
├── RailYardApiApplication
├── common
│   ├── error/        ErrorCode, ApiErrorResponse, exceptions
│   └── web/          CorrelationIdFilter, WebAutoConfiguration, OpenApi config
├── yard/             Yard entity, repository, DTOs, service, controller
├── track/            Track + capability entities, DTOs, service, controller
├── train/            Train + required-capability entities, DTOs, service, controller
├── reservation/      package-info (placeholder for next slice)
├── recommendation/   package-info (placeholder for next slice)
└── audit/            package-info (placeholder for next slice)
```

### 3.3 Database — `backend/src/main/resources/db/migration/`

- **V1__enable_btree_gist.sql** enables the `btree_gist` extension. Required because the GiST exclusion constraint in V3 needs equality support on UUID track identifiers.
- **V2__create_core_schema.sql** creates seven tables with foreign keys, business checks, unique constraints, optimistic-lock `version` columns, and six performance indexes.

  | Table | Purpose | Key constraints and indexes |
  |---|---|---|
  | `yards` | Rail-yard identity, location, IANA time zone, active flag | unique `code`; not-blank checks |
  | `tracks` | Length, purpose, status, setup/clearance buffers, yard FK | unique `(yard_id, code)`; positive length, non-negative buffers, enumerated purpose/status; indexes on `(yard_id, status)` |
  | `track_capabilities` | Normalised capability set per track | composite PK `(track_id, capability)`, enumerated values |
  | `trains` | Train identity, length, service type, priority, origin/destination | unique `train_number`; `priority ∈ [1,5]`; positive length; enumerated `service_type` |
  | `train_required_capabilities` | Normalised capability requirements per train | composite PK `(train_id, capability)` |
  | `track_reservations` | Buffered window, status, audit fields, `(track_id, yard_id)` composite FK | operation/status enums, interval order checks, 24-hour buffer bound check, indexes on `(yard_id, scheduled_arrival)`, `(track_id, occupied_from, occupied_until)`, `(train_id, scheduled_arrival)`, and `status` |
  | `audit_events` | Append-only change log with JSON before/after snapshots | UUID required, non-blank entity_type and action |

- **V3__prevent_overlapping_reservations.sql** creates the authoritative concurrency invariant:

  ```sql
  ALTER TABLE track_reservations
      ADD CONSTRAINT ex_reservations_no_track_overlap
      EXCLUDE USING gist (
          yard_id WITH =,
          track_id WITH =,
          tstzrange(occupied_from, occupied_until, '[)') WITH &&
      )
      WHERE (status IN ('PLANNED', 'ACTIVE'));
  ```

  This is a partial GiST exclusion constraint. Two `PLANNED` or `ACTIVE` reservations on the same track and yard cannot overlap. The half-open `[start, end)` range means adjacent reservations are allowed. `CANCELLED` and `COMPLETED` reservations do not block. Including `yard_id` makes the constraint self-defending even if a future migration changes the composite FK.

- The buffer-bound CHECK constraint `occupied_until <= scheduled_departure + INTERVAL '24 hours'` prevents a buggy or compromised writer from setting `occupied_until = 'infinity'` to bypass the GiST constraint.

- `scripts/demo-data.sql` is an idempotent script that inserts synthetic yards, tracks, capabilities, trains, and one reservation using fixed UUIDs. Verified repeatable: running it twice leaves the row counts unchanged.

### 3.4 Yard master-data API

`com.justrk07.railyard.yard`:

- `Yard` entity: `id` (UUID), `code` (uppercase regex), `name`, `location`, `timeZone` (IANA name), `active`, `createdAt`, `updatedAt`, `version` (optimistic lock). Factory method `Yard.create(...)` and `update(...)` method own the creation/update rules.
- `YardRepository` extends `JpaRepository<Yard, UUID>` with `findByCode` and a stable `findAllByOrderByCodeAsc`.
- `YardRequest` record with Jakarta validation (`@NotBlank`, `@Size`, `@Pattern` for code).
- `YardResponse` record with `YardResponse.from(Yard)`.
- `YardService` throws `DuplicateResourceException` on duplicate code, `ResourceNotFoundException` on missing id, returns a `Page<Yard>` ordered by code.
- `YardController` exposes `GET /api/yards`, `GET /api/yards/{yardId}`, `POST /api/yards`, `PATCH /api/yards/{yardId}` and emits OpenAPI annotations.

### 3.5 Track and capability master-data API

`com.justrk07.railyard.track`:

- `Track` entity with `@ElementCollection` for capabilities, three enums (`TrackStatus`, `TrackPurpose`, `TrackCapability`), buffer fields, optimistic locking, and a `changeStatus(TrackStatus)` method.
- `TrackRepository` returns tracks ordered by code for a yard or ordered by `(yardId, code)` globally; `findByYardIdAndCode` is used for duplicate-code detection.
- `TrackRequest` validates `usableLengthMeters ≥ 1`, non-negative buffers, and a non-null purpose.
- `TrackResponse` exposes the capability set in a stable order.
- `TrackService` validates the parent yard, prevents duplicate codes within a yard, handles status-only PATCH.
- `TrackController` exposes `GET /api/yards/{yardId}/tracks`, `GET /api/tracks`, `GET /api/tracks/{trackId}`, `POST /api/yards/{yardId}/tracks`, `PUT /api/tracks/{trackId}`, and `PATCH /api/tracks/{trackId}/status`.

### 3.6 Train master-data API

`com.justrk07.railyard.train`:

- `Train` entity with `@ElementCollection` of `train_required_capabilities`.
- `TrackCapabilityRef` is a re-export of the capability enumeration in the train module so the train code does not depend on the track module's enum directly.
- `TrainRepository` has `searchByNumber` using `LIKE LOWER(...)` for case-insensitive search.
- `TrainRequest` validates priority in `[1,5]`, uppercase train-number regex, positive length, non-blank origin/destination.
- `TrainService` and `TrainController` mirror the yard pattern.
- The reservation history endpoint `GET /api/trains/{trainId}/reservations` is not yet implemented (planned for the scheduling slice).

### 3.7 Cross-cutting error and web layer

- `ErrorCode` is a stable, append-only enum: `VALIDATION_FAILED`, `RESOURCE_NOT_FOUND`, `DUPLICATE_RESOURCE`, `BUSINESS_RULE_VIOLATION`, `TRACK_RESERVATION_CONFLICT`, `INTERNAL_ERROR`.
- `ApiErrorResponse` is the wire envelope:

  ```json
  {
    "code": "DUPLICATE_RESOURCE",
    "message": "Yard code APX-1 already exists",
    "correlationId": "aea52377-8fc3-42d5-a38b-0a7485878cc4",
    "timestamp": "2026-07-23T05:54:50.323974302Z",
    "path": "/api/yards",
    "fieldErrors": null,
    "details": null
  }
  ```

- `CorrelationIdFilter` is a `OncePerRequestFilter` ordered `HIGHEST_PRECEDENCE`. It reads the `X-Correlation-Id` header (or generates a UUID), sets it on the response, and pushes it onto the SLF4J MDC under the key `correlationId`. The exception handler reads this MDC entry to include the correlation id in the error envelope.
- `WebAutoConfiguration` is a `@RestControllerAdvice` that maps every documented exception type to the envelope above. `MethodArgumentTypeMismatchException` is mapped to 400 so that an invalid enum query parameter such as `?status=BOGUS` returns a validation error, not a 500.
- `PageSerializationConfiguration` enables `PageSerializationMode.VIA_DTO` so Spring Data page responses are serialised as a stable `{ content, page: { size, number, totalElements, totalPages } }` envelope. The Spring 4.0 default `PageImpl` serialisation is undocumented and unstable across versions; this is fixed.
- `OpenApiConfiguration` registers the title, description, version, and a portfolio-only license.

### 3.8 Backend tests

- `DatabaseMigrationTests` (Testcontainers, PostgreSQL 17) verifies:
  1. All seven expected tables exist.
  2. The `ex_reservations_no_track_overlap` constraint exists.
  3. The constraint keys include `yard_id` (the safety net we added in the review fix).
  4. Adjacent half-open windows are accepted.
  5. An overlapping window on the same track raises `DataIntegrityViolationException`.
- `MasterDataApiIntegrationTests` (Testcontainers + MockMvc) covers:
  1. Yard create and get with the `X-Correlation-Id` response header.
  2. Duplicate yard code → 409 with `code: DUPLICATE_RESOURCE` and a non-empty `correlationId`.
  3. Missing yard → 404 with `code: RESOURCE_NOT_FOUND`.
  4. Empty payload → 400 with `code: VALIDATION_FAILED` and per-field error messages.
  5. Track create inside a yard with capability round-trip.
  6. Track status PATCH with an invalid enum value → 400 (via the type-mismatch handler).
  7. Train create + search-by-number round trip.
  8. Train full update replacing every field.
  9. Train priority outside `[1,5]` → 400.
- `RailYardApiApplicationTests` is the Spring Boot context test.
- Total: 12 tests passing.

## 4. Frontend — what is implemented

### 4.1 Stack and configuration

- **Angular 21.2** with the current `application` builder, strict TypeScript, standalone components, **Vitest 4** as the unit-test runner, and **Prettier** formatting.
- SCSS styles, routing, and a global design token system in `src/styles.scss`.
- Three `@fontsource` packages provide the typefaces: `barlow-condensed` (display), `ibm-plex-sans` (body), `ibm-plex-mono` (utility/captions/data).
- `angular.json` component-style set to `scss`. Production budgets are 500 kB initial bundle warning / 1 MB error and 6 kB warning / 8 kB error per component style sheet.

### 4.2 Operations shell (`src/app/app.ts`)

The application shell is the deliberate design centrepiece. It defines a navigation model (`Yard occupancy`, `Assign a train`, `Trains`, `Tracks`, `Reservations`) and renders:

- A `topbar` with the `RY` brand mark, the product name `Rail Yard Control`, and an environment indicator.
- A `sidebar` with a primary navigation rail that uses a vertical rail-tie pattern as a background motif. The active route is highlighted with a `worklight-500` accent.
- A `main` workspace region that hosts `<router-outlet>`.
- A skip-to-content link for keyboard users.
- Responsive behaviour: at 760 px the sidebar collapses into a horizontal scroll list.

Tokens in `src/styles.scss`:

- Palette: `ballast-950`, `rail-900/800/700/600`, `steel-400/300/200`, `fog-100`, `paper`, `ink`, `muted-ink`, `signal-teal`, `signal-ready`, `worklight-500/100`, `danger`.
- Type scale: display `Barlow Condensed`, body `IBM Plex Sans`, utility `IBM Plex Mono`.
- Grid-line background pattern at 2.5 rem spacing for the workspace.

The 1440 × 1000 desktop screenshot at `/tmp/rail-yard-occupancy.png` and the 390 × 844 mobile screenshot at `/tmp/rail-yard-assignment-mobile.png` (generated during this development pass) show the rendered shell, both verified to render without console errors. The Playwright-driven smoke test reports the correct URL, h1, and navigation link count after clicking the `Assign a train` link.

### 4.3 Route placeholders (`src/app/features/workspace-placeholder/`)

Each of the five routes (`/occupancy`, `/assignment`, `/trains`, `/tracks`, `/reservations`) is currently a `WorkspacePlaceholder` component that reads its `data` from the route and renders:

- A page heading with eyebrow label, large display title, and a single-sentence description.
- A `Module planned` badge.
- A `Delivery card` listing the three concrete capabilities the eventual module will provide.
- A `Current engineering focus` engineering note card with a numbered sequence (Schema → API → Workflow) that points back to the milestone ordering.

Vitest unit tests verify the placeholder reads and renders route-supplied data correctly.

### 4.4 Frontend tests

Two test files, three tests passing, building under 55 kB total.

## 5. Delivery infrastructure — what is implemented

### 5.1 Docker Compose

- `compose.yaml` defines three services: `db` (postgres:17-alpine), `backend` (build from `backend/`), `frontend` (build from `frontend/`).
- The `db` service uses health checks (`pg_isready`) and a persistent named volume.
- The `backend` service depends on `db` being healthy; the `frontend` service depends on `backend` being healthy.
- Environment-driven defaults (`POSTGRES_*`, `BACKEND_PORT`, `FRONTEND_PORT`) match `.env.example`.
- `docker compose config --quiet` validates the file.

### 5.2 Dockerfiles

- `backend/Dockerfile` uses a multi-stage build with `maven:3.9-eclipse-temurin-21` to build with the Maven Wrapper and a JRE Alpine runtime. Creates a non-root `app` user, copies the Spring Boot fat jar, and runs as `java -jar /app/app.jar`. Includes `curl` for the health check.
- `frontend/Dockerfile` uses `node:24-alpine` to build the Angular app with `npm ci` (fetch retries configured) and `npm run build`, then ships the static assets through `nginx:1.29-alpine`. Health check is `/healthz`.
- Both Dockerfiles have matching `.dockerignore` files that keep node_modules, target, dist, and IDE files out of the build context.

### 5.3 Nginx proxy (`frontend/nginx.conf`)

The containerized frontend proxies backend traffic at:

- `/api/` → `http://backend:8080`
- `/actuator/` → `http://backend:8080`
- `/v3/api-docs/` → `http://backend:8080` (trailing-slash prefix)
- `/swagger-ui/` → `http://backend:8080` (trailing-slash prefix)

Static assets fall through to the Angular SPA. The trailing slashes were added in the post-review fix so that Swagger UI sub-resources and the YAML/JSON schema exports resolve correctly.

### 5.4 GitHub Actions

`.github/workflows/ci.yml` defines three jobs:

- **backend**: JDK 21, caches Maven, runs `./mvnw verify`.
- **frontend**: Node 24, runs `npm ci`, `npm test -- --watch=false`, `npm run build`, `npm audit --omit=dev --audit-level=high`.
- **containers**: runs `docker compose config --quiet` and `docker compose build backend frontend`.

The workflow has not yet been exercised because the repository is not yet pushed.

### 5.5 Documentation

- `README.md` — quickstart, technology, repository layout, local development, principles, milestone summary, data and affiliation notice.
- `docs/architecture.md` — system shape, module map, scheduling request flow, runtime configuration.
- `docs/er-diagram.md` — Mermaid ER diagram of all seven tables, design notes for capability normalisation, the `(track_id, yard_id)` composite FK, and the JSON-in-audit-events decision.
- `docs/concurrency.md` — interval semantics, the half-open overlap rule, why an application-level check is insufficient, the planned transaction flow, and the verification path.
- `docs/local-development.md` — requirements, full-stack start, demo data, backend verification in Docker, frontend verification, reset instructions.
- `docs/demo-script.md` — foundation checkpoint and target-MVP demonstration narrative.
- `docs/adr/0001-modular-monolith.md` — ADR accepting the modular monolith.
- `docs/adr/0002-time-window-semantics.md` — ADR for UTC half-open windows.
- `docs/adr/0003-conflict-protection.md` — ADR for PostgreSQL-enforced overlap protection.
- `docs/adr/0004-authentication-deferred.md` — ADR accepting that auth is deferred from the MVP.
- `docs/ROADMAP.md` — granular task checklist across five milestones, with the foundation and master-data items checked off.

## 6. Verified state — what actually works

The following has been observed during this development pass and is reproducible from a clean clone:

- **Backend unit and integration tests**: 12 tests passing, including the PostgreSQL-only schema test and the MockMvc API integration tests. `./mvnw verify` succeeds in the Java 21 container.
- **Frontend tests**: 3 tests passing.
- **Frontend production build**: 238.58 kB initial bundle, no budget warnings, no production `npm audit` vulnerabilities.
- **Docker Compose stack**: `docker compose up --build` starts cleanly with a fresh DB volume; backend health, frontend health, frontend proxy to backend, and OpenAPI all return 200.
- **Live master-data API**:
  - `POST /api/yards` returns 201 with a UUID, `code`, `name`, etc.
  - Duplicate `POST` returns 409 with `code: DUPLICATE_RESOURCE` and a correlation id.
  - `GET /api/yards/00000000-...-99` returns 404 with `code: RESOURCE_NOT_FOUND`.
  - Empty body returns 400 with `code: VALIDATION_FAILED` and per-field error messages.
  - `GET /api/yards?size=10&page=0` returns the stable page envelope.
  - `POST /api/yards/{id}/tracks` and `POST /api/trains` round-trip with the new entities.
- **Browser smoke test**: Playwright clicks from `/occupancy` to `/assignment` and reports the correct h1 and five navigation links with zero console errors.
- **Demo data**: `scripts/demo-data.sql` is idempotent (running twice leaves row counts unchanged).
- **Database constraint**: Adjacent reservations are accepted; overlapping reservations on the same track are rejected by the GiST constraint, which is verified by the Testcontainers test.

## 7. What is not yet implemented

The following are scoped but not yet coded:

- Reservation lifecycle (PLANNED, ACTIVE, COMPLETED, CANCELLED) and the `TimeWindow` half-open overlap utility.
- Recommendation service with `PriorityQueue` ranking, exclusion reason codes, and explanations.
- Yard occupancy timeline UI.
- Angular reactive forms for the assignment and track/train administration workflows.
- Authentication, role-based authorization, and real user management.
- Audit event emission wired into the API (the table and ADR are in place).
- AWS deployment (only the local Compose stack is built).
- Performance test against a larger synthetic reservation dataset.
- LICENSE file selection and CI first-run validation (only possible after the first push).

These are tracked in `docs/ROADMAP.md`.

## 8. Reproducible commands

```bash
# Backend tests in Java 21 Docker (host has no JDK 21)
docker run --rm --network host \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$PWD/backend:/workspace" \
  -v rail-yard-maven-cache:/root/.m2 \
  -w /workspace maven:3.9-eclipse-temurin-21 \
  ./mvnw verify

# Frontend tests and build
npm --prefix frontend ci
npm --prefix frontend test -- --watch=false
npm --prefix frontend run build

# Full stack with fresh database
cp .env.example .env
POSTGRES_PORT=55432 docker compose up --build

# Idempotent demo data
docker compose exec -T db psql -U rail_yard -d rail_yard < scripts/demo-data.sql
```

## 9. How this maps to the Wabtec JD

| JD line | Where the evidence lives |
|---|---|
| Programming (C/C++/Java/Python) | `backend/` Java 21 / Spring Boot |
| Frontend (Angular/JS) | `frontend/` Angular 21 / TypeScript |
| RDBMS | Flyway migrations, exclusion constraint, buffer CHECK, indexes, joins |
| Data structures and algorithms | `TimeWindow` (planned), `PriorityQueue` recommendation (planned), overlap detection (covered by `permitsAdjacentWindowsAndRejectsAnOverlapOnTheSameTrack`) |
| Unit and integration tests | JUnit + Mockito + Testcontainers + Spring MockMvc + Angular Vitest |
| Cloud (AWS) as an add-on | Docker Compose + Dockerfiles in place; AWS deployment is a later milestone |
| Basic SDLC | Issues-and-ADRs documentation, conventional commit grouping ready, CI pipeline, versioned migrations |
| Take ownership of module delivery | Each feature module owns its entities, DTOs, services, controllers, and tests |
| Assist with development, troubleshooting, and deployment | Reproducible Docker stack, health checks, correlation IDs, structured error contract |
| Be an excellent problem solver | The review-driven fixes (Spring Boot 4.0 namespaces, nginx trailing-slash prefixes, GiST yard_id, 24h buffer bound, page DTO, type-mismatch handler) are concrete examples of identifying and fixing real defects |

This document describes the state of the repository at the end of the master-data API slice. The next slice will add the `TimeWindow` utility, the recommendation service, and the transactional reservation workflow.
