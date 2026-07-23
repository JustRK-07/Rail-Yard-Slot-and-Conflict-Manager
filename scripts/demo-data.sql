-- Deterministic synthetic data for the portfolio demo.
-- Apply only after Flyway migrations have completed:
-- psql "$DATABASE_URL" -f scripts/demo-data.sql

BEGIN;

INSERT INTO yards (id, code, name, location, time_zone)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'YD-A',
    'North Junction Demonstration Yard',
    'Pune, India',
    'Asia/Kolkata'
)
ON CONFLICT (code) DO NOTHING;

INSERT INTO tracks (
    id, yard_id, code, usable_length_m, purpose, status,
    setup_buffer_minutes, clearance_buffer_minutes
)
VALUES
    ('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000001', 'T01', 450, 'ARRIVAL', 'OPERATIONAL', 5, 5),
    ('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000001', 'T02', 900, 'STAGING', 'OPERATIONAL', 10, 10),
    ('00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000001', 'T03', 700, 'STAGING', 'MAINTENANCE', 10, 10),
    ('00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000001', 'T04', 850, 'MULTIPURPOSE', 'OPERATIONAL', 10, 10),
    ('00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000001', 'T05', 1100, 'DEPARTURE', 'OPERATIONAL', 5, 10)
ON CONFLICT (yard_id, code) DO NOTHING;

INSERT INTO track_capabilities (track_id, capability)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'DIESEL'),
    ('00000000-0000-0000-0000-000000000102', 'DIESEL'),
    ('00000000-0000-0000-0000-000000000102', 'HEAVY_FREIGHT'),
    ('00000000-0000-0000-0000-000000000103', 'DIESEL'),
    ('00000000-0000-0000-0000-000000000104', 'DIESEL'),
    ('00000000-0000-0000-0000-000000000104', 'HEAVY_FREIGHT'),
    ('00000000-0000-0000-0000-000000000105', 'DIESEL'),
    ('00000000-0000-0000-0000-000000000105', 'HEAVY_FREIGHT')
ON CONFLICT DO NOTHING;

INSERT INTO trains (
    id, train_number, length_m, service_type, priority, origin, destination
)
VALUES
    ('00000000-0000-0000-0000-000000000201', 'FR-101', 775, 'FREIGHT', 2, 'Pune', 'Nagpur'),
    ('00000000-0000-0000-0000-000000000202', 'FR-202', 640, 'FREIGHT', 3, 'Mumbai', 'Hyderabad')
ON CONFLICT (train_number) DO NOTHING;

INSERT INTO train_required_capabilities (train_id, capability)
VALUES
    ('00000000-0000-0000-0000-000000000201', 'DIESEL'),
    ('00000000-0000-0000-0000-000000000201', 'HEAVY_FREIGHT'),
    ('00000000-0000-0000-0000-000000000202', 'DIESEL')
ON CONFLICT DO NOTHING;

INSERT INTO track_reservations (
    id, yard_id, track_id, train_id, operation_type,
    scheduled_arrival, scheduled_departure, occupied_from, occupied_until,
    status, notes, created_by, updated_by
)
VALUES (
    '00000000-0000-0000-0000-000000000301',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000102',
    '00000000-0000-0000-0000-000000000202',
    'STAGING',
    '2026-08-10T09:00:00Z',
    '2026-08-10T10:00:00Z',
    '2026-08-10T08:50:00Z',
    '2026-08-10T10:10:00Z',
    'PLANNED',
    'Synthetic reservation used to demonstrate conflict filtering.',
    'demo.dispatcher',
    'demo.dispatcher'
)
ON CONFLICT (id) DO NOTHING;

COMMIT;
