# Architecture

## System shape

The application is a modular monolith with three deployable processes:

```mermaid
flowchart LR
    Browser[Angular operations UI] -->|JSON over HTTP| API[Spring Boot API]
    API -->|JPA and SQL| DB[(PostgreSQL)]
    API --> Health[Actuator health]
    API --> Docs[OpenAPI]
```

A modular monolith keeps transaction and scheduling rules in one process while preserving explicit feature boundaries. It avoids the deployment and data-consistency overhead of microservices for a portfolio-scale system.

## Backend modules

| Module | Responsibility |
|---|---|
| `yard` | Yard identity, location, time zone, and active status |
| `track` | Track dimensions, purpose, status, buffers, and capabilities |
| `train` | Train dimensions, service data, priority, and requirements |
| `reservation` | Conflict-safe assignment and lifecycle rules |
| `recommendation` | Candidate filtering, ranking, and explanations |
| `audit` | Append-only records for operational changes |
| `common` | Shared errors, validation, time semantics, and API concerns |

Features own their controllers, services, persistence code, and DTOs. Controllers do not contain scheduling rules, and JPA entities are not exposed as API responses.

## Scheduling request flow

```mermaid
sequenceDiagram
    actor Dispatcher
    participant UI as Angular UI
    participant Recommendation as Recommendation service
    participant Reservation as Reservation service
    participant DB as PostgreSQL

    Dispatcher->>UI: Enter train and requested window
    UI->>Recommendation: Request track recommendations
    Recommendation->>DB: Query compatible tracks and conflicts
    Recommendation-->>UI: Ranked and excluded tracks with reasons
    Dispatcher->>UI: Confirm one track
    UI->>Reservation: Create reservation
    Reservation->>DB: Revalidate and insert in one transaction
    DB-->>Reservation: Accept or reject overlap
    Reservation-->>UI: 201 Created or 409 Conflict
```

A recommendation is advisory. Reservation creation always performs an authoritative recheck because availability can change between recommendation and confirmation.

## Runtime configuration

- The API reads connection details from environment variables.
- Flyway applies schema changes before JPA validation.
- PostgreSQL stores all operational timestamps as `TIMESTAMPTZ`.
- The UI will display the selected yard's IANA time zone while the API uses UTC `Instant` values.
- Docker Compose provides reproducible local services; GitHub Actions runs the same builds and tests.
