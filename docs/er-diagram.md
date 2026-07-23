# Relational model

```mermaid
erDiagram
    YARDS ||--o{ TRACKS : contains
    TRACKS ||--o{ TRACK_CAPABILITIES : supports
    TRAINS ||--o{ TRAIN_REQUIRED_CAPABILITIES : requires
    YARDS ||--o{ TRACK_RESERVATIONS : schedules
    TRACKS ||--o{ TRACK_RESERVATIONS : receives
    TRAINS ||--o{ TRACK_RESERVATIONS : occupies

    YARDS {
        uuid id PK
        varchar code UK
        varchar name
        varchar location
        varchar time_zone
        boolean active
        bigint version
    }

    TRACKS {
        uuid id PK
        uuid yard_id FK
        varchar code UK
        integer usable_length_m
        varchar purpose
        varchar status
        integer setup_buffer_minutes
        integer clearance_buffer_minutes
        bigint version
    }

    TRACK_CAPABILITIES {
        uuid track_id PK,FK
        varchar capability PK
    }

    TRAINS {
        uuid id PK
        varchar train_number UK
        integer length_m
        varchar service_type
        smallint priority
        varchar origin
        varchar destination
        boolean active
        bigint version
    }

    TRAIN_REQUIRED_CAPABILITIES {
        uuid train_id PK,FK
        varchar capability PK
    }

    TRACK_RESERVATIONS {
        uuid id PK
        uuid yard_id FK
        uuid track_id FK
        uuid train_id FK
        varchar operation_type
        timestamptz scheduled_arrival
        timestamptz scheduled_departure
        timestamptz occupied_from
        timestamptz occupied_until
        varchar status
        bigint version
    }

    AUDIT_EVENTS {
        uuid id PK
        varchar entity_type
        uuid entity_id
        varchar action
        varchar actor_id
        timestamptz occurred_at
        jsonb before_values
        jsonb after_values
        uuid correlation_id
    }
```

## Design notes

- Capability requirements are normalized rather than stored as comma-separated values or opaque JSON.
- `(track_id, yard_id)` has a composite foreign key so a reservation cannot claim a yard different from its track's yard.
- Requested and effective occupancy windows are both stored. Effective values include setup and clearance buffers and are used for conflict protection.
- `audit_events` uses JSON only for before/after snapshots; operational entities remain relational and queryable.
- Database check constraints protect lengths, priorities, statuses, and interval order independently of application validation.

The implemented source of truth is `backend/src/main/resources/db/migration/`.
