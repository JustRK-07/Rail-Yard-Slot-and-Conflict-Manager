CREATE TABLE yards (
    id UUID PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(120) NOT NULL,
    location VARCHAR(160) NOT NULL,
    time_zone VARCHAR(80) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_yards_code UNIQUE (code),
    CONSTRAINT ck_yards_code_not_blank CHECK (btrim(code) <> ''),
    CONSTRAINT ck_yards_name_not_blank CHECK (btrim(name) <> '')
);

CREATE TABLE tracks (
    id UUID PRIMARY KEY,
    yard_id UUID NOT NULL,
    code VARCHAR(20) NOT NULL,
    usable_length_m INTEGER NOT NULL,
    purpose VARCHAR(24) NOT NULL,
    status VARCHAR(24) NOT NULL,
    setup_buffer_minutes INTEGER NOT NULL DEFAULT 0,
    clearance_buffer_minutes INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_tracks_yard FOREIGN KEY (yard_id) REFERENCES yards (id),
    CONSTRAINT uq_tracks_yard_code UNIQUE (yard_id, code),
    CONSTRAINT uq_tracks_id_yard UNIQUE (id, yard_id),
    CONSTRAINT ck_tracks_code_not_blank CHECK (btrim(code) <> ''),
    CONSTRAINT ck_tracks_usable_length CHECK (usable_length_m > 0),
    CONSTRAINT ck_tracks_setup_buffer CHECK (setup_buffer_minutes >= 0),
    CONSTRAINT ck_tracks_clearance_buffer CHECK (clearance_buffer_minutes >= 0),
    CONSTRAINT ck_tracks_purpose CHECK (
        purpose IN ('ARRIVAL', 'STAGING', 'DEPARTURE', 'MAINTENANCE', 'MULTIPURPOSE')
    ),
    CONSTRAINT ck_tracks_status CHECK (status IN ('OPERATIONAL', 'MAINTENANCE', 'CLOSED'))
);

CREATE TABLE track_capabilities (
    track_id UUID NOT NULL,
    capability VARCHAR(32) NOT NULL,
    CONSTRAINT pk_track_capabilities PRIMARY KEY (track_id, capability),
    CONSTRAINT fk_track_capabilities_track FOREIGN KEY (track_id) REFERENCES tracks (id) ON DELETE CASCADE,
    CONSTRAINT ck_track_capability CHECK (
        capability IN ('DIESEL', 'ELECTRIFIED', 'HEAVY_FREIGHT', 'HAZARDOUS_MATERIAL', 'LOADING', 'PASSENGER')
    )
);

CREATE TABLE trains (
    id UUID PRIMARY KEY,
    train_number VARCHAR(32) NOT NULL,
    length_m INTEGER NOT NULL,
    service_type VARCHAR(24) NOT NULL,
    priority SMALLINT NOT NULL DEFAULT 3,
    origin VARCHAR(120) NOT NULL,
    destination VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_trains_number UNIQUE (train_number),
    CONSTRAINT ck_trains_number_not_blank CHECK (btrim(train_number) <> ''),
    CONSTRAINT ck_trains_length CHECK (length_m > 0),
    CONSTRAINT ck_trains_priority CHECK (priority BETWEEN 1 AND 5),
    CONSTRAINT ck_trains_service_type CHECK (
        service_type IN ('FREIGHT', 'PASSENGER', 'MAINTENANCE', 'MIXED')
    )
);

CREATE TABLE train_required_capabilities (
    train_id UUID NOT NULL,
    capability VARCHAR(32) NOT NULL,
    CONSTRAINT pk_train_required_capabilities PRIMARY KEY (train_id, capability),
    CONSTRAINT fk_train_capabilities_train FOREIGN KEY (train_id) REFERENCES trains (id) ON DELETE CASCADE,
    CONSTRAINT ck_train_capability CHECK (
        capability IN ('DIESEL', 'ELECTRIFIED', 'HEAVY_FREIGHT', 'HAZARDOUS_MATERIAL', 'LOADING', 'PASSENGER')
    )
);

CREATE TABLE track_reservations (
    id UUID PRIMARY KEY,
    yard_id UUID NOT NULL,
    track_id UUID NOT NULL,
    train_id UUID NOT NULL,
    operation_type VARCHAR(24) NOT NULL,
    scheduled_arrival TIMESTAMPTZ NOT NULL,
    scheduled_departure TIMESTAMPTZ NOT NULL,
    occupied_from TIMESTAMPTZ NOT NULL,
    occupied_until TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes VARCHAR(1000),
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_reservations_track_yard FOREIGN KEY (track_id, yard_id) REFERENCES tracks (id, yard_id),
    CONSTRAINT fk_reservations_train FOREIGN KEY (train_id) REFERENCES trains (id),
    CONSTRAINT ck_reservations_operation CHECK (
        operation_type IN ('ARRIVAL', 'STAGING', 'DEPARTURE', 'MAINTENANCE')
    ),
    CONSTRAINT ck_reservations_status CHECK (
        status IN ('PLANNED', 'ACTIVE', 'COMPLETED', 'CANCELLED')
    ),
    CONSTRAINT ck_reservations_schedule CHECK (scheduled_departure > scheduled_arrival),
    CONSTRAINT ck_reservations_occupancy CHECK (occupied_until > occupied_from),
    CONSTRAINT ck_reservations_arrival_buffer CHECK (occupied_from <= scheduled_arrival),
    CONSTRAINT ck_reservations_departure_buffer CHECK (occupied_until >= scheduled_departure),
    CONSTRAINT ck_reservations_buffer_bounds CHECK (
        occupied_until <= scheduled_departure + INTERVAL '24 hours'
        AND occupied_from >= scheduled_arrival - INTERVAL '24 hours'
    )
);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(60) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(60) NOT NULL,
    actor_id VARCHAR(120) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    before_values JSONB,
    after_values JSONB,
    correlation_id UUID NOT NULL,
    CONSTRAINT ck_audit_entity_type_not_blank CHECK (btrim(entity_type) <> ''),
    CONSTRAINT ck_audit_action_not_blank CHECK (btrim(action) <> '')
);

CREATE INDEX idx_tracks_yard_status ON tracks (yard_id, status);
CREATE INDEX idx_reservations_yard_arrival ON track_reservations (yard_id, scheduled_arrival);
CREATE INDEX idx_reservations_track_window ON track_reservations (track_id, occupied_from, occupied_until);
CREATE INDEX idx_reservations_train_arrival ON track_reservations (train_id, scheduled_arrival);
CREATE INDEX idx_reservations_status ON track_reservations (status);
CREATE INDEX idx_audit_entity_history ON audit_events (entity_type, entity_id, occurred_at DESC);
