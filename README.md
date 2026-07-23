# Rail Yard Slot and Conflict Manager

A full-stack rail-operations portfolio project for assigning trains to suitable yard tracks without creating schedule conflicts.

> **Project status:** foundation under active development. The repository currently establishes the architecture, database model, local development environment, and automated test pipeline before the scheduling workflow is implemented.

## What the system will do

A dispatcher will be able to:

- Inspect current and upcoming track occupancy.
- Request track recommendations for a train and time window.
- Understand why a track was recommended or excluded.
- Create, reschedule, activate, complete, or cancel a reservation.
- Receive a clear conflict response when a track is already occupied.
- Review an audit trail for operational changes.

The central rule uses half-open time intervals, `[arrival, departure)`, plus configured setup and clearance buffers. PostgreSQL provides the final protection against concurrent overlapping reservations.

## Technology

- **Frontend:** Angular 21, TypeScript, RxJS, SCSS, Vitest
- **Backend:** Java 21, Spring Boot 4.0, Maven
- **Database:** PostgreSQL 17, Flyway, GiST exclusion constraints
- **Testing:** JUnit, Spring integration tests, Testcontainers, Vitest
- **Delivery:** Docker Compose and GitHub Actions

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

The backend is organized by business feature (`yard`, `track`, `train`, `reservation`, `recommendation`, and `audit`) rather than as separate microservices.

## Repository layout

```text
backend/       Spring Boot API and database migrations
frontend/      Angular operations interface
docs/          Architecture, ER model, concurrency notes, and ADRs
scripts/       Deterministic synthetic demo data
compose.yaml   Local PostgreSQL, API, and frontend services
PROJECT_SPEC.md
```

## Local development

### Docker-first setup

The host does not need Maven or PostgreSQL when Docker is available.

```bash
cp .env.example .env
docker compose up --build
```

Expected local endpoints after the services are healthy:

- Frontend: <http://localhost:4200>
- Backend health: <http://localhost:8080/actuator/health>
- OpenAPI UI: <http://localhost:8080/swagger-ui.html>

### Frontend only

```bash
cd frontend
npm ci
npm start
```

### Backend tests with Java 21 through Docker

```bash
docker run --rm \
  -v "$PWD/backend:/workspace" \
  -w /workspace \
  maven:3.9-eclipse-temurin-21 \
  ./mvnw verify
```

## Development principles

- Store timestamps in UTC and display them in the yard's IANA time zone.
- Keep authoritative validation on the server.
- Use explicit API DTOs instead of exposing persistence entities.
- Apply every schema change through Flyway.
- Test against PostgreSQL rather than substituting an in-memory database.
- Report only measured coverage and performance.
- Never commit credentials or a real `.env` file.

## Planned milestones

1. Buildable backend, frontend, PostgreSQL schema, Docker, and CI.
2. Yard, track, and train master-data APIs.
3. Recommendation and conflict-safe reservation workflow.
4. Angular occupancy and dispatcher workflows.
5. Hardening, documentation, measured test evidence, and optional AWS deployment.

See [`PROJECT_OVERVIEW.md`](PROJECT_OVERVIEW.md) for what is actually built in the current slice, [`PROJECT_SPEC.md`](PROJECT_SPEC.md) for the target product specification, and [`docs/ROADMAP.md`](docs/ROADMAP.md) for the verified implementation checklist.

## Data and affiliation notice

All yards, tracks, trains, schedules, and operational events in this repository are synthetic. This independent portfolio project is not affiliated with, endorsed by, or based on proprietary systems from Wabtec Corporation or any railway operator.
