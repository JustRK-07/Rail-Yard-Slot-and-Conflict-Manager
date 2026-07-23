ALTER TABLE track_reservations
    ADD CONSTRAINT ex_reservations_no_track_overlap
    EXCLUDE USING gist (
        yard_id WITH =,
        track_id WITH =,
        tstzrange(occupied_from, occupied_until, '[)') WITH &&
    )
    WHERE (status IN ('PLANNED', 'ACTIVE'));
