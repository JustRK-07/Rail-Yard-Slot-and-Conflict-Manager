# Rail Yard Slot and Conflict Manager

## 1. Project summary

**Rail Yard Slot and Conflict Manager** is a full-stack web application that helps rail-yard dispatchers assign arriving and departing trains to suitable tracks without creating schedule conflicts. Dispatchers can view track occupancy, request track recommendations, reserve a track, reschedule or cancel an assignment, and review an audit trail of operational changes.

The application uses a modern Angular frontend, a Java Spring Boot REST API, and PostgreSQL. Its key engineering feature is a scheduling module that filters incompatible tracks, detects overlapping time intervals, ranks valid alternatives, and safely handles two dispatchers attempting to reserve the same track at the same time.

This is a fictional portfolio system built with synthetic data. It should not imply access to, endorsement by, or use of proprietary Wabtec systems.

## 2. Why this project is valuable

This project is more than a CRUD application. It demonstrates:

- A real transportation-domain problem related to rail operations.
- Modern Angular and TypeScript frontend development.
- Java and Spring Boot backend development.
- Relational schema design, SQL joins, constraints, indexes, and transactions.
- Practical use of interval algorithms and a priority queue.
- Unit, repository, API integration, frontend, and optional end-to-end testing.
- SDLC practices through requirements, issues, pull requests, CI, versioned migrations, and deployment.
- Ownership of a complete software module from design through delivery.

## 3. Business problem

A rail yard contains several tracks with different usable lengths, purposes, and operational states. Trains arrive and depart according to planned time windows. A dispatcher must select a track that:

1. Belongs to the correct yard.
2. Is operational during the requested period.
3. Is long enough for the train.
4. Supports the requested operation, such as arrival, staging, departure, or maintenance.
5. Has no overlapping reservation, including any required safety buffer.

Manually checking all tracks is slow and error-prone. Two dispatchers may also try to reserve the same track concurrently. The proposed system centralizes the schedule, automatically detects conflicts, recommends suitable tracks, and records every change for traceability.

## 4. Primary objective

Build a production-style scheduling module that can answer the following question reliably:

> Given a train, yard, arrival time, departure time, and operational purpose, which tracks are valid, which one is the best choice, and can that assignment be committed without a conflict?

## 5. Intended users

### Dispatcher

- Views current and upcoming yard occupancy.
- Searches for a train.
- Requests recommended tracks.
- Creates, reschedules, or cancels a reservation.
- Sees a clear explanation when an assignment is rejected.

### Yard manager

- Performs all dispatcher actions.
- Creates and updates yards and tracks.
- Marks tracks operational, closed, or under maintenance.
- Reviews schedule changes and audit history.

### Administrator — optional extension

- Manages application users and roles.
- Configures operational-purpose and capability reference data.

Authentication and role management can be postponed until the scheduling workflow is complete. The internship JD is better demonstrated by a reliable core module than by spending most of the project on login functionality.

## 6. Core user journey

1. A dispatcher opens the yard occupancy page and selects a date range.
2. The application retrieves tracks and active reservations for the selected yard.
3. The dispatcher selects an unassigned train and enters its requested arrival and departure times.
4. The backend validates the request and searches for compatible tracks.
5. Conflicting, closed, too-short, or incompatible tracks are removed.
6. Remaining tracks are ranked and returned with human-readable reasons.
7. The dispatcher selects a recommendation and confirms the assignment.
8. The backend checks the conflict again inside a database transaction.
9. If the track is still free, the reservation and audit event are committed together.
10. If another user took the slot first, the API returns `409 Conflict` and fresh alternatives are displayed.

The second conflict check is important: a recommendation is only a snapshot and must not guarantee that a track will still be available when the dispatcher submits the reservation.

## 7. Functional requirements

### FR-01: Yard management

The yard manager can create, view, update, and deactivate a yard. A yard has a unique code, name, location, and time zone.

### FR-02: Track management

The yard manager can create and update tracks. Each track has:

- A unique code within its yard.
- A usable length in metres.
- An operational purpose or supported capabilities.
- A status such as `OPERATIONAL`, `MAINTENANCE`, or `CLOSED`.
- Optional setup and clearance buffers.

### FR-03: Train management

Users can register and update trains with a unique train number, length, service type, priority, origin, destination, and planned yard visit.

### FR-04: Occupancy search

Users can list track reservations by yard and time range. Results include the train, track, planned arrival/departure, reservation status, and last update time.

### FR-05: Track recommendations

Given a train and requested window, the application returns compatible tracks ordered by suitability. Every recommendation includes an explanation, such as:

- `Track T04 is operational and has 75 m spare capacity.`
- `Track T02 was excluded because reservation R-119 overlaps by 20 minutes.`
- `Track T03 was excluded because its usable length is too short.`

### FR-06: Reservation creation

A dispatcher can reserve one recommended track. The API must reject:

- Invalid or zero-length time ranges.
- Unknown or inactive trains.
- Closed or incompatible tracks.
- Tracks shorter than the train.
- Overlapping active reservations.

### FR-07: Conflict response

A rejected overlapping reservation returns HTTP `409 Conflict` with:

- A stable error code.
- A user-friendly message.
- The conflicting reservation ID and time window, where permitted.
- A correlation ID for troubleshooting.

### FR-08: Rescheduling

A dispatcher can change the track or time window of a planned reservation. The same validation and concurrency rules used for creation apply to rescheduling.

### FR-09: Cancellation

A dispatcher can cancel a planned reservation while preserving its history. Reservations should normally be status-updated rather than physically deleted.

### FR-10: Status lifecycle

A reservation follows controlled transitions:

```text
PLANNED -> ACTIVE -> COMPLETED
    |         |
    +------> CANCELLED
```

Invalid transitions, such as `COMPLETED -> ACTIVE`, are rejected.

### FR-11: Audit trail

The system records reservation creation, rescheduling, status changes, and cancellation with actor, timestamp, action, and before/after values.

### FR-12: Validation and error handling

The frontend and backend both validate input. Backend validation remains authoritative. Errors use a consistent response format and do not expose stack traces or database details.

## 8. Scope boundaries

### Minimum viable product

- One or more yards and their tracks.
- Train master data.
- Reservation search and occupancy view.
- Recommendation engine.
- Conflict-safe create, reschedule, and cancel operations.
- Audit records.
- Unit and integration tests.
- Docker Compose and CI pipeline.

### Deliberately out of MVP scope

- Live GPS or signalling-system integration.
- Automatic train control.
- Machine-learning predictions.
- Real railway safety certification.
- Payment, ticketing, passenger, or crew management.
- A microservice architecture.
- Kafka or real-time streaming infrastructure.

Keeping these items out of the MVP makes the project achievable and keeps attention on the JD requirements.

## 9. Business rules

1. All timestamps are stored in UTC; the UI displays the selected yard's time zone.
2. A requested departure must be later than its arrival.
3. Time windows use **half-open intervals**: `[arrival, departure)`. Therefore, a reservation ending at 10:00 does not conflict with one starting at 10:00 unless a configured buffer applies.
4. Safety/setup buffers extend the occupied period before and after the requested window.
5. A train must fit within the track's usable length.
6. Only operational tracks can accept new reservations.
7. Track capabilities must contain the capability required by the train operation.
8. `CANCELLED` reservations do not block a track.
9. `PLANNED` and `ACTIVE` reservations block a track.
10. Every accepted scheduling change and its audit record must commit in one transaction.
11. The server performs a fresh conflict check during commit, even if a recommendation was generated seconds earlier.
12. Concurrent requests for the same track and time window must result in at most one successful reservation.

## 10. Conflict-detection logic

Two buffered reservations overlap when:

```text
existing.start < requested.end
AND
existing.end > requested.start
```

They do not overlap when one ends exactly when the other begins under the half-open interval rule.

### Example

Existing reservation:

```text
09:00 <= time < 10:00
```

| Requested time | Result |
|---|---|
| 08:00–09:00 | Allowed without a buffer |
| 10:00–11:00 | Allowed without a buffer |
| 09:30–10:30 | Conflict |
| 08:45–09:15 | Conflict |
| 09:00–10:00 | Conflict |

If a 10-minute clearance buffer is configured, the effective existing occupancy becomes 08:50–10:10.

### Database protection

Application checks alone are vulnerable to concurrent requests. The final implementation should combine:

- A Spring transaction around validation, reservation insertion, and audit insertion.
- A database-level protection strategy.
- An integration test that submits two competing reservations concurrently.

A strong PostgreSQL implementation can use a range column and an exclusion constraint so active reservations for the same track cannot overlap. A simpler first version can use explicit locking and an indexed overlap query, followed by the database constraint as an advanced milestone.

## 11. Recommendation algorithm

### Step 1: Validate input

Validate the train, yard, operation type, time order, maximum reservation duration, and any configured buffer.

### Step 2: Filter infeasible tracks

Remove tracks that are:

- In another yard.
- Inactive, closed, or under maintenance.
- Shorter than the train.
- Missing a required capability.
- Already occupied during the buffered time range.

### Step 3: Rank valid tracks

Rank candidates using a deterministic score. A sensible MVP ranking order is:

1. Exact operation-purpose match.
2. Smallest non-negative unused track length.
3. Lowest scheduling fragmentation around the requested slot.
4. Stable track-code tie-breaker.

A Java `PriorityQueue<TrackCandidate>` can return the best candidates without sorting unrelated records. The API should return both the score components and plain-language reasons so the ranking is explainable.

### Step 4: Commit safely

When the user accepts a recommendation, repeat authoritative validation in a transaction. Never trust a recommendation ID as proof that availability is unchanged.

## 12. Suggested architecture

```text
Angular application
  |-- Yard occupancy feature
  |-- Train feature
  |-- Scheduling and recommendation feature
  |-- Track administration feature
  |-- Audit feature
          |
          | HTTPS / JSON REST API
          v
Spring Boot application
  |-- Web/controller layer
  |-- Request validation and exception handling
  |-- Scheduling service
  |-- Recommendation service
  |-- Yard/train/track services
  |-- Persistence repositories
  |-- Audit service
          |
          v
PostgreSQL
  |-- Relational tables and constraints
  |-- Flyway migrations
  |-- Indexed conflict queries
  |-- Transaction/concurrency protection
```

Use a **modular monolith**, not microservices. It is easier to build, test, deploy, and explain while still demonstrating good separation of responsibilities.

## 13. Recommended technology stack

### Frontend

- Current supported Angular version.
- TypeScript.
- Angular Router.
- Reactive Forms.
- Angular Material or a small custom component system.
- RxJS for API state and request handling.
- Jasmine/Karma or Jest for unit/component tests.
- Playwright or Cypress as an optional end-to-end layer.

### Backend

- Java 21.
- Spring Boot 3.x.
- Spring Web.
- Spring Data JPA.
- Jakarta Bean Validation.
- Spring Security only when authentication is added.
- Maven or Gradle.
- springdoc-openapi for API documentation.

### Database

- PostgreSQL.
- Flyway for version-controlled schema migrations.
- Testcontainers for integration tests against real PostgreSQL behavior.

### Delivery

- Git and GitHub.
- Docker and Docker Compose.
- GitHub Actions.
- Optional AWS deployment using an application service plus RDS PostgreSQL.

## 14. Proposed relational schema

### `yards`

- `id` — primary key.
- `code` — globally unique business code.
- `name`.
- `location`.
- `time_zone`.
- `active`.
- `created_at`, `updated_at`.

### `tracks`

- `id` — primary key.
- `yard_id` — foreign key to `yards`.
- `code` — unique within one yard.
- `usable_length_m`.
- `purpose`.
- `status`.
- `setup_buffer_minutes`.
- `clearance_buffer_minutes`.
- `version` — for optimistic-locking support where applicable.
- `created_at`, `updated_at`.

### `track_capabilities`

- `track_id` — foreign key to `tracks`.
- `capability`.
- Composite primary key on `track_id` and `capability`.

This normalized table avoids storing comma-separated capability values.

### `trains`

- `id` — primary key.
- `train_number` — unique.
- `length_m`.
- `service_type`.
- `priority`.
- `origin`.
- `destination`.
- `active`.
- `created_at`, `updated_at`.

### `track_reservations`

- `id` — primary key.
- `yard_id` — foreign key to `yards` for efficient yard queries.
- `track_id` — foreign key to `tracks`.
- `train_id` — foreign key to `trains`.
- `operation_type`.
- `scheduled_arrival`.
- `scheduled_departure`.
- `occupied_from` and `occupied_until` after applying buffers.
- `status`.
- `notes`.
- `created_by`, `updated_by`.
- `created_at`, `updated_at`.
- `version`.

### `audit_events`

- `id` — primary key.
- `entity_type`.
- `entity_id`.
- `action`.
- `actor_id`.
- `occurred_at`.
- `before_values`.
- `after_values`.
- `correlation_id`.

Core entities remain relational. JSON can be used only for audit snapshots where the values are not part of operational querying.

### Optional `users`

- `id`.
- `email` — unique.
- `display_name`.
- `role`.
- `active`.
- Authentication fields appropriate to the selected login strategy.

## 15. Important database constraints and indexes

- Unique index on `yards.code`.
- Unique index on `(tracks.yard_id, tracks.code)`.
- Unique index on `trains.train_number`.
- Check constraint that train and track lengths are positive.
- Check constraint that `scheduled_departure > scheduled_arrival`.
- Foreign keys for all entity relationships.
- Index on `(track_id, occupied_from, occupied_until)` for conflict searches.
- Index on `(yard_id, scheduled_arrival)` for occupancy views.
- Index on `(train_id, scheduled_arrival)` for train history.
- Database-level overlap protection for active reservations as an advanced requirement.

The README should include an ER diagram and explain why each major index exists.

## 16. REST API outline

### Yards and tracks

```text
GET    /api/yards
POST   /api/yards
GET    /api/yards/{yardId}/tracks
POST   /api/yards/{yardId}/tracks
PUT    /api/tracks/{trackId}
PATCH  /api/tracks/{trackId}/status
```

### Trains

```text
GET    /api/trains?query={text}
POST   /api/trains
GET    /api/trains/{trainId}
PUT    /api/trains/{trainId}
GET    /api/trains/{trainId}/reservations
```

### Scheduling

```text
POST   /api/scheduling/recommendations
POST   /api/reservations
GET    /api/reservations/{reservationId}
PUT    /api/reservations/{reservationId}/schedule
POST   /api/reservations/{reservationId}/activate
POST   /api/reservations/{reservationId}/complete
POST   /api/reservations/{reservationId}/cancel
```

### Operations view and audit

```text
GET    /api/yards/{yardId}/occupancy?from={timestamp}&to={timestamp}
GET    /api/audit-events?entityType={type}&entityId={id}
```

### Recommendation request example

```json
{
  "yardId": 1,
  "trainId": 42,
  "operationType": "STAGING",
  "scheduledArrival": "2026-08-10T09:00:00Z",
  "scheduledDeparture": "2026-08-10T11:00:00Z",
  "limit": 5
}
```

### Recommendation response example

```json
{
  "requestId": "rec-7f54",
  "generatedAt": "2026-08-10T08:40:00Z",
  "recommendations": [
    {
      "trackId": 14,
      "trackCode": "T04",
      "rank": 1,
      "usableLengthM": 850,
      "unusedLengthM": 75,
      "reasons": [
        "No overlapping reservation",
        "Supports STAGING operations",
        "75 m spare capacity"
      ]
    }
  ],
  "excludedTracks": [
    {
      "trackId": 12,
      "trackCode": "T02",
      "reasonCode": "TIME_CONFLICT",
      "message": "Occupied during part of the requested interval"
    }
  ]
}
```

The generated recommendation is advisory. Reservation creation performs validation again.

## 17. Frontend pages

### Yard occupancy page

- Yard selector.
- Date/time-range selector.
- One row per track with current and upcoming reservations.
- Status labels for planned, active, completed, and cancelled assignments.
- Filters by train, track, status, and operation type.
- A refresh action and clear loading, empty, and error states.

### New assignment workflow

- Select or search for a train.
- Enter operation type and requested times.
- Request recommendations.
- Review ranked candidates and exclusion reasons.
- Confirm a selected track.
- Handle a stale recommendation or `409 Conflict` without losing form input.

### Train page

- Search and create trains.
- Display train details and reservation history.

### Track administration page

- Create and edit track details.
- Update operational status.
- Display future reservations before allowing a closure.

### Reservation details page

- Display train, yard, track, times, buffers, status, and audit history.
- Provide valid reschedule, cancel, activate, or complete actions based on current status.

### Audit page

- Filter by entity, action, actor, and date.
- Display before/after changes in a readable format.

## 18. Backend module structure

A package-by-feature structure is preferable to placing every controller or service in one global folder:

```text
com.example.railyard
  common/
    errors/
    time/
  yard/
  track/
  train/
  scheduling/
    api/
    application/
    domain/
    persistence/
  reservation/
  audit/
  security/          # optional phase
```

Important classes could include:

- `SchedulingRecommendationService`.
- `ReservationConflictDetector`.
- `TrackCandidateRanker`.
- `ReservationService`.
- `ReservationStatusPolicy`.
- `AuditService`.

Keep controllers thin. Business rules belong in domain/application services rather than Angular components or JPA repositories.

## 19. Testing strategy

### Backend unit tests

Test pure business logic without starting Spring:

- Two intervals with a one-minute overlap conflict.
- Adjacent half-open intervals do not conflict.
- A configured buffer turns adjacent intervals into a conflict.
- A track shorter than the train is excluded.
- A closed track is excluded.
- A track lacking a capability is excluded.
- Candidate ranking is deterministic.
- Invalid status transitions are rejected.
- Cancellation removes a reservation from future conflict checks.

### Repository integration tests

Use Testcontainers with PostgreSQL rather than replacing it with an in-memory database:

- Flyway migrations apply to an empty database.
- Conflict queries find every overlap pattern.
- Cancelled reservations are ignored.
- Yard occupancy joins return correct train and track information.
- Constraints reject invalid lengths and time ranges.
- Index-supported queries behave correctly with realistic seed data.

### API integration tests

Start the Spring application and call real endpoints:

- A valid assignment returns `201 Created`.
- An overlapping assignment returns `409 Conflict`.
- A malformed request returns `400 Bad Request` with field errors.
- A missing train returns `404 Not Found`.
- A reschedule applies the same conflict rules as creation.
- Reservation and audit event commit together.
- A failed audit or reservation operation rolls back the entire transaction.
- Two concurrent requests for one slot produce one success and one conflict.

### Angular tests

- Reactive form validation.
- Recommendation service request and response mapping.
- Candidate and exclusion-reason rendering.
- Conflict response handling.
- Occupancy filtering.
- Buttons enabled only for valid status transitions.

### Optional end-to-end tests

- Create a train, request a recommendation, reserve a track, and verify occupancy.
- Attempt a conflicting reservation and verify the recovery workflow.
- Reschedule and cancel a reservation.

### Coverage policy

Set a reasonable target, such as 80% for backend business-logic packages, but report only measured coverage. More important than a headline number is covering conflict boundaries, transactions, and concurrency behavior.

## 20. Non-functional requirements

### Reliability

- Database constraints protect critical scheduling invariants.
- Transactions prevent partial updates.
- Retry behavior must not create duplicate reservations.

### Performance

- Paginate long lists.
- Restrict occupancy queries to a required date range.
- Add indexes based on actual query patterns.
- Measure before making performance claims.

### Security

- Validate all server-side inputs.
- Use parameterized repository queries.
- Do not expose stack traces or secrets.
- Keep credentials in environment variables or a secrets service.
- Apply role checks if authentication is implemented.

### Observability

- Structured application logs.
- Correlation IDs propagated from API request to error response and audit event.
- Health endpoint for deployment checks.
- Optional metrics for request counts and conflict rejections.

### Usability and accessibility

- Do not communicate status by color alone.
- Support keyboard navigation and visible focus states.
- Provide clear loading, empty, success, validation, and conflict states.
- Confirm destructive-looking actions such as cancellation.

## 21. SDLC workflow

### Requirements

Create GitHub Issues for user stories and technical work. Each user story should include acceptance criteria.

Example:

> As a dispatcher, I want incompatible tracks removed from recommendations so that I cannot accidentally choose an unsafe or unusable assignment.

Acceptance criteria:

- Closed tracks are excluded.
- Tracks shorter than the train are excluded.
- Overlapping tracks are excluded.
- Each exclusion contains a reason code.
- Unit and API tests cover the behavior.

### Development workflow

- Keep `main` releasable.
- Work in short feature branches.
- Open pull requests that link to issues.
- Run formatting, backend tests, frontend tests, and builds in CI.
- Require a green CI run before merge, even if the repository has one developer.
- Use conventional or otherwise consistent commit messages.

### Documentation

Create:

- `README.md`.
- ER diagram.
- Architecture diagram.
- OpenAPI documentation.
- Seed-data explanation.
- At least two Architecture Decision Records.
- A short demo script.

Useful ADR topics:

1. Why a modular monolith was selected over microservices.
2. Why PostgreSQL/Testcontainers were selected instead of an in-memory test database.
3. How half-open intervals and buffers are represented.
4. How concurrent track assignment is protected.

## 22. Suggested repository layout

```text
rail-yard-slot-conflict-manager/
  backend/
    src/main/java/
    src/main/resources/db/migration/
    src/test/java/
    pom.xml
  frontend/
    src/app/
    src/environments/
    package.json
  docs/
    architecture.md
    er-diagram.md
    api.md
    adr/
    demo-script.md
  scripts/
    seed-demo-data.sh
  .github/workflows/
    ci.yml
  compose.yaml
  .env.example
  README.md
  LICENSE
```

Do not commit real `.env` files, passwords, access keys, build output, or IDE-specific files.

## 23. Delivery milestones

### Milestone 1: Requirements and database foundation

- Define user stories and acceptance criteria.
- Create architecture and ER diagrams.
- Initialize backend, frontend, and CI.
- Create Flyway migrations and seed data.
- Implement yard, track, and train APIs.

**Demo:** create a yard, tracks, and trains and query them from PostgreSQL.

### Milestone 2: Conflict engine and reservations

- Implement interval and buffer rules.
- Implement reservation creation and status lifecycle.
- Add database concurrency protection.
- Add unit, repository, and API integration tests.

**Demo:** accept a valid reservation and reject boundary, overlap, and concurrent conflicts.

### Milestone 3: Recommendation engine

- Filter infeasible tracks.
- Implement deterministic ranking with explainable reasons.
- Add recommendation endpoint and tests.

**Demo:** submit one scheduling request and explain every included and excluded track.

### Milestone 4: Angular operations workflow

- Build occupancy, recommendation, reservation, and detail pages.
- Add validation and error handling.
- Add frontend tests and optional end-to-end tests.

**Demo:** complete the dispatcher journey entirely from the browser.

### Milestone 5: Delivery and presentation

- Add Docker Compose.
- Finalize GitHub Actions.
- Add OpenAPI documentation and screenshots.
- Run and record measured tests.
- Optionally deploy on AWS.
- Prepare a three-to-five-minute demo.

**Demo:** clone, launch, test, and demonstrate the system using documented commands.

## 24. Definition of done

A feature is complete only when:

- Acceptance criteria are satisfied.
- Backend and frontend validation are implemented where relevant.
- Unit and/or integration tests cover important success and failure paths.
- Database changes use a versioned migration.
- API documentation is updated.
- CI passes.
- No credentials or generated build files are committed.
- The pull request or issue explains the decision and evidence.

The complete project is portfolio-ready when a reviewer can clone it, start it with documented commands, load demo data, execute tests, and reproduce the conflict scenario.

## 25. Demonstration scenario

Use synthetic data:

- Yard `YD-A` with tracks `T01` through `T05`.
- Tracks with different lengths, purposes, and statuses.
- One train that is too long for `T01`.
- One track under maintenance.
- One existing reservation on `T02` from 09:00 to 10:00.
- A new request from 09:30 to 11:00.

During the demo:

1. Open the occupancy page.
2. Request a track for the new train.
3. Show why `T01`, `T02`, and the maintenance track were excluded.
4. Show valid tracks ordered by score.
5. Reserve the top recommendation.
6. Attempt an overlapping reservation and show the `409 Conflict` response.
7. Reschedule the train to a different valid track.
8. Show the audit history.
9. Run the test suite and show the CI result.

This tells a complete engineering story rather than merely showing several forms.

## 26. JD mapping

| JD requirement | Evidence in this project |
|---|---|
| Basic SDLC knowledge | Issues, acceptance criteria, branches, pull requests, migrations, ADRs, CI, and milestone delivery |
| Assist with development, troubleshooting, and deployment | Structured logs, correlation IDs, integration tests, Docker, documentation, and optional AWS deployment |
| Take ownership of module delivery | Scheduling module designed, implemented, tested, documented, and demonstrated end to end |
| Collaborate and report progress | GitHub project board, concise pull requests, milestone notes, and demo presentation |
| Java/Python/C/C++ knowledge | Java 21 and Spring Boot backend |
| Angular/JavaScript knowledge | Angular, TypeScript, RxJS, reactive forms, and frontend tests |
| RDBMS knowledge | PostgreSQL schema, foreign keys, joins, constraints, indexes, migrations, and transactions |
| AWS knowledge as an add-on | Optional application deployment and RDS PostgreSQL |
| Data structures and algorithms | Interval overlap detection, candidate filtering, comparator logic, and priority queue ranking |
| Unit and integration tests | JUnit/Mockito unit tests, Testcontainers repository tests, API integration tests, and Angular tests |
| Technical discussion or proof of concept | ADRs, architecture diagram, OpenAPI contract, conflict/concurrency demo, and presentation |
| Rail/ports product relevance | Rail-yard track scheduling and operations workflow |

## 27. Short description for a resume or portfolio

> Developed a full-stack rail-yard scheduling application that recommends compatible tracks and prevents overlapping train reservations. Built the frontend with Angular and TypeScript, implemented transactional scheduling services in Java and Spring Boot, and designed a PostgreSQL schema with constraints, joins, indexes, and versioned migrations. Verified interval boundaries and concurrent booking behavior through unit and Testcontainers-based integration tests, with automated builds and tests in GitHub Actions.

Change this wording to past tense only after the relevant functionality exists.

## 28. Resume bullet templates

Use verified facts and replace placeholders only after measuring them:

- Built a rail-yard scheduling platform with Angular, Spring Boot, and PostgreSQL, implementing explainable track recommendations and interval-based reservation conflict detection.
- Designed a normalized relational schema with versioned migrations, transactional reservation updates, and database-level concurrency protection to prevent overlapping track assignments.
- Added JUnit, Testcontainers, and Angular tests covering boundary conditions, API workflows, rollback behavior, and concurrent requests, achieving **[measured]%** coverage in **[measured package/scope]**.
- Automated frontend and backend validation through GitHub Actions and deployed the application to **[actual AWS service, if completed]**.

Do not invent user counts, accuracy, coverage, latency improvements, or cloud deployment.

## 29. Interview talking points

Be prepared to explain:

1. Why the intervals are half-open.
2. Why checking for conflicts only in Java is insufficient under concurrency.
3. How transactions and PostgreSQL constraints work together.
4. Why Testcontainers is more reliable here than an H2-only test suite.
5. How candidates are filtered and ranked.
6. Why the ranking returns explanations.
7. Which SQL indexes support the main queries.
8. Why a modular monolith is appropriate for this scope.
9. Which part failed during development and how logs or tests isolated the problem.
10. What would change if real-time train events or multiple yards were added.

## 30. Future extensions

Implement these only after the MVP is reliable:

- Track-maintenance windows represented as another conflict source.
- A yard topology graph and switching-distance cost.
- Server-sent events or WebSockets for occupancy updates.
- CSV import for schedules with row-level validation reports.
- Delay propagation and rule-based ETA estimates.
- Notification workflow for conflicts or track closures.
- AWS deployment with RDS, health checks, logs, and a documented infrastructure diagram.
- Performance test using a larger synthetic reservation dataset.

A finished, tested MVP is more valuable than several unfinished advanced features.
