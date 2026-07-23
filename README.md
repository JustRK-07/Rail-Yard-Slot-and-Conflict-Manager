# Rail Yard Slot and Conflict Manager

A full-stack rail-operations application that helps dispatchers assign trains to suitable yard tracks without creating schedule conflicts.

> **Project status (as of the last commit on `main`):**
>
> - The **foundation** and **master-data API** slices are implemented and committed.
> - 12 backend tests pass against PostgreSQL 17 (Testcontainers).
> - 3 frontend tests pass; production build passes.
> - Local Docker Compose stack starts cleanly and serves master-data APIs.
> - The reservation, recommendation, and Angular feature-workflow slices are still pending.
>
> See [`PROJECT_OVERVIEW.md`](PROJECT_OVERVIEW.md) for the verified build, [`PROJECT_SPEC.md`](PROJECT_SPEC.md) for the target product, and [`docs/ROADMAP.md`](docs/ROADMAP.md) for the granular task list.

## What is implemented today

### Backend (Java 21 / Spring Boot 4.0.7)

- Modular monolith organised by feature: `yard`, `track`, `train`, `common` (with `reservation`, `recommendation`, and `audit` placeholders for the next slice).
- Normalised PostgreSQL 17 schema with three Flyway migrations, foreign keys, optimistic-locking `version` columns, business checks, six performance indexes, and a partial GiST exclusion constraint that enforces non-overlapping track reservations for `PLANNED` and `ACTIVE` rows.
- An additional CHECK constraint bounds setup and clearance buffers to 24 hours so a compromised writer cannot bypass the GiST constraint.
- `ErrorCode` enum, RFC-style `ApiErrorResponse` envelope, `CorrelationIdFilter`, and `WebAutoConfiguration` (`@RestControllerAdvice`) that maps every documented exception to the envelope with the right HTTP status.
- Stable page serialization (`PageSerializationMode.VIA_DTO`) so list responses do not depend on undocumented Spring Data internals.
- Master-data APIs:
  - `GET /api/yards`, `GET /api/yards/{id}`, `POST /api/yards`, `PATCH /api/yards/{id}`
  - `GET /api/yards/{yardId}/tracks`, `GET /api/tracks?yardId=`, `GET /api/tracks/{id}`, `POST /api/yards/{yardId}/tracks`, `PUT /api/tracks/{trackId}`, `PATCH /api/tracks/{trackId}/status?status=…`
  - `GET /api/trains?query=…`, `GET /api/trains/{id}`, `POST /api/trains`, `PUT /api/trains/{id}`
- OpenAPI metadata exposed at `/v3/api-docs` and the Swagger UI at `/swagger-ui`.

### Frontend (Angular 21 / TypeScript / Vitest)

- Standalone-component operations shell with the `RY` brand mark, topbar, sidebar navigation rail, and main workspace region.
- Five routes (`/occupancy`, `/assignment`, `/trains`, `/tracks`, `/reservations`) currently rendered through a `WorkspacePlaceholder` component that states the module's planned capabilities.
- Design tokens for the ballast / rail / steel / worklight palette, three typefaces via `@fontsource` (Barlow Condensed display, IBM Plex Sans body, IBM Plex Mono utility), and a 2.5 rem grid-line background.
- Responsive collapse at 760 px and reduced-motion support.
- Vitest unit tests for the shell and the placeholder.

### Database and demo data

- `scripts/demo-data.sql` inserts a deterministic yard, five tracks with capabilities, two trains, and one reservation using fixed UUIDs. It is idempotent (verified by re-running the script and confirming unchanged row counts).

### Delivery

- `compose.yaml` orchestrates PostgreSQL 17 (with health checks and a persistent volume), the Spring Boot backend (built from `backend/Dockerfile`), and the Angular frontend served by nginx (built from `frontend/Dockerfile`).
- `frontend/nginx.conf` proxies `/api/`, `/actuator/`, `/v3/api-docs/`, and `/swagger-ui/` to the backend with trailing-slash prefix matches.
- `.github/workflows/ci.yml` runs backend verify, frontend lint/test/build/audit, and Compose build verification.

## What is not implemented yet

- The `TimeWindow` half-open overlap utility and full reservation lifecycle (`PLANNED → ACTIVE → COMPLETED`, with `CANCELLED` reachable from `PLANNED` or `ACTIVE`).
- The `PriorityQueue`-based recommendation engine with stable reason codes and explanation messages.
- Transactional reservation creation with atomic audit-event insertion, including the SQLSTATE `23P01` → `409 TRACK_RESERVATION_CONFLICT` mapping.
- Angular reactive forms for the assignment, train, and track administration workflows, plus the yard occupancy timeline.
- Authentication, role-based authorization, real user management, and any user-facing audit timeline.
- Cloud deployment (only the local Docker Compose stack is built).
- LICENSE file (deliberately left for the user to choose).
- The first GitHub Actions run (only possible after the first push).

## Technology

- **Frontend:** Angular 21, TypeScript, RxJS, SCSS, Vitest, Prettier, `@fontsource` typography.
- **Backend:** Java 21, Spring Boot 4.0.7, Spring Web MVC, Spring Data JPA, Spring Validation, Spring Flyway, Spring Actuator, springdoc OpenAPI.
- **Database:** PostgreSQL 17, Flyway, GiST exclusion constraints, `btree_gist` extension.
- **Testing:** JUnit Jupiter, Spring MockMvc, Testcontainers PostgreSQL, AssertJ, Vitest 4.
- **Delivery:** Docker Compose, multi-stage Dockerfiles, nginx, GitHub Actions.

## Architecture

```text
Angular browser application
          |
          | JSON REST API
          v
Spring Boot modular monolith
          |
          | JPA + Flyway
          v
PostgreSQL
```

The backend is organised by business feature (`yard`, `track`, `train`, `common`, with `reservation`, `recommendation`, and `audit` placeholders) rather than as separate microservices. API controllers are thin; business rules live in services; JPA entities are not exposed as responses.

## Repository layout

```text
backend/                Spring Boot API, migrations, and tests
frontend/               Angular operations interface
docs/                   Architecture, ER, concurrency, demo, ADRs, roadmap
scripts/                Deterministic synthetic demo data
compose.yaml            Local PostgreSQL, backend, and frontend services
.github/workflows/ci.yml
PROJECT_SPEC.md         Target product specification (30 sections)
PROJECT_OVERVIEW.md     What is actually built in the current slice
README.md               This file
.editorconfig
.env.example
.gitignore
```

## Local development

### Full stack with Docker Compose

The host only needs Docker. The build runs JDK 21 and Node 24 inside their respective images.

```bash
cp .env.example .env
docker compose up --build
```

If port 5432 is already in use on the host, override the host port:

```bash
POSTGRES_PORT=55432 docker compose up --build
```

After the services are healthy:

| Service | URL |
|---|---|
| Frontend operations shell | <http://localhost:4200/occupancy> |
| Frontend nginx health | <http://localhost:4200/healthz> |
| Backend health | <http://localhost:8080/actuator/health> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |
| Swagger UI | <http://localhost:8080/swagger-ui/index.html> |
| Frontend → backend proxy | <http://localhost:4200/api/yards> |

### Load the deterministic demo data

```bash
docker compose exec -T db psql -U "${POSTGRES_USER:-rail_yard}" -d "${POSTGRES_DB:-rail_yard}" < scripts/demo-data.sql
```

### Reset the local database

```bash
docker compose down --volumes
```

### Backend tests through Java 21 in Docker

The host only needs Docker and the Maven Wrapper:

```bash
cd backend
docker run --rm --network host \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$PWD:/workspace" \
  -v rail-yard-maven-cache:/root/.m2 \
  -w /workspace maven:3.9-eclipse-temurin-21 \
  ./mvnw verify
```

The Docker socket is mounted because Testcontainers creates an isolated PostgreSQL container for the integration tests.

### Frontend tests and production build

```bash
cd frontend
npm ci
npm test -- --watch=false
npm run build
npm audit --omit=dev --audit-level=high
```

## Development principles

- All timestamps are stored in UTC `TIMESTAMPTZ` and Java `Instant`; the UI will display the selected yard's IANA time zone.
- Authoritative validation lives on the server; the client performs best-effort validation only.
- API responses use explicit DTOs; JPA entities are not serialised.
- Every schema change is versioned through Flyway.
- Tests run against PostgreSQL via Testcontainers rather than an in-memory database.
- Coverage, performance, and cloud-deployment claims are only published after they are measured.
- No credentials, `.env`, or generated build output are committed.

## Local development environment notes

- **Frontend** listens on `4200` and proxies to the backend on `8080` through the nginx configuration in `frontend/nginx.conf`.
- **Backend** listens on `8080` and uses environment variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SERVER_PORT`) for configuration; defaults match `compose.yaml`.
- **Database** exposes a single database; Flyway applies three migrations on startup. To reset, run `docker compose down --volumes`.

## Commit history (current)

```text
bc1e94c docs: reflect master-data slice status in README, overview, roadmap, and ADRs
15afa4e docs: add demo data script and project overview for the implemented slice
9f9381b feat(frontend): scaffold Angular 21 dispatcher operations shell
0470465 feat(backend): add Spring Boot 4.0 modular foundation and PostgreSQL schema
f756aa2 chore: initialize repository with project metadata and documentation
```

The remote is `git@github.com:JustRK-07/Rail-Yard-Slot-and-Conflict-Manager.git`. SSH on this host currently returns `Permission denied (publickey)`, so the push is deferred until you configure GitHub SSH access.

## Roadmap and tracking

The granular task list lives in [`docs/ROADMAP.md`](docs/ROADMAP.md). Milestone 0 (foundation) and Milestone 1 (master-data APIs) are checked off. The remaining milestones are:

- **Milestone 2 — Scheduling and reservations**: `TimeWindow` utility, recommendation service, transactional reservation creation, conflict translation, lifecycle endpoints, concurrent-request tests.
- **Milestone 3 — Dispatcher workflow**: typed API client, assignment reactive form, ranked-recommendation rendering, reservation detail and audit-history pages, accessible yard occupancy timeline, complete state set, end-to-end tests.
- **Milestone 4 — Portfolio delivery**: OpenAPI examples and screenshots, measured coverage, security review, optional cloud deployment, and resume content that only references verified facts.

## Data and affiliation notice

All yards, tracks, trains, schedules, and operational events in this repository are synthetic. This independent portfolio project is not affiliated with, endorsed by, or based on proprietary systems from any railway operator or equipment vendor.