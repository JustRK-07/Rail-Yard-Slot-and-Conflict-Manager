package com.justrk07.railyard.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.justrk07.railyard.TestcontainersConfiguration;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class DatabaseMigrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsTheExpectedRelationalSchemaAndOverlapConstraint() {
        Integer tableCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN (
                      'yards', 'tracks', 'track_capabilities', 'trains',
                      'train_required_capabilities', 'track_reservations', 'audit_events'
                  )
                """, Integer.class);
        Integer constraintCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM pg_constraint
                WHERE conname = 'ex_reservations_no_track_overlap'
                """, Integer.class);
        Integer constraintKeyCount = jdbcTemplate.queryForObject("""
                SELECT count(*)
                FROM pg_constraint con
                CROSS JOIN LATERAL unnest(con.conkey) AS ord
                JOIN pg_attribute attr
                  ON attr.attrelid = con.conrelid
                 AND attr.attnum = ord
                WHERE con.conname = 'ex_reservations_no_track_overlap'
                  AND attr.attname = 'yard_id'
                """, Integer.class);

        assertThat(tableCount).isEqualTo(7);
        assertThat(constraintCount).isEqualTo(1);
        assertThat(constraintKeyCount).isEqualTo(1);
    }

    @Test
    void permitsAdjacentWindowsAndRejectsAnOverlapOnTheSameTrack() {
        UUID yardId = UUID.randomUUID();
        UUID trackId = UUID.randomUUID();
        UUID firstTrainId = UUID.randomUUID();
        UUID adjacentTrainId = UUID.randomUUID();
        UUID overlappingTrainId = UUID.randomUUID();

        insertYard(yardId);
        insertTrack(trackId, yardId);
        insertTrain(firstTrainId, "TEST-A-" + firstTrainId.toString().substring(0, 8));
        insertTrain(adjacentTrainId, "TEST-B-" + adjacentTrainId.toString().substring(0, 8));
        insertTrain(overlappingTrainId, "TEST-C-" + overlappingTrainId.toString().substring(0, 8));

        OffsetDateTime nine = OffsetDateTime.parse("2026-08-10T09:00:00Z");
        OffsetDateTime ten = OffsetDateTime.parse("2026-08-10T10:00:00Z");
        OffsetDateTime eleven = OffsetDateTime.parse("2026-08-10T11:00:00Z");

        insertReservation(UUID.randomUUID(), yardId, trackId, firstTrainId, nine, ten);
        insertReservation(UUID.randomUUID(), yardId, trackId, adjacentTrainId, ten, eleven);

        assertThatThrownBy(() -> insertReservation(
                UUID.randomUUID(),
                yardId,
                trackId,
                overlappingTrainId,
                OffsetDateTime.parse("2026-08-10T09:30:00Z"),
                OffsetDateTime.parse("2026-08-10T10:30:00Z")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertYard(UUID yardId) {
        jdbcTemplate.update("""
                INSERT INTO yards (id, code, name, location, time_zone)
                VALUES (?, ?, 'Integration Test Yard', 'Test Location', 'UTC')
                """, yardId, "Y-" + yardId.toString().substring(0, 8));
    }

    private void insertTrack(UUID trackId, UUID yardId) {
        jdbcTemplate.update("""
                INSERT INTO tracks (id, yard_id, code, usable_length_m, purpose, status)
                VALUES (?, ?, ?, 900, 'STAGING', 'OPERATIONAL')
                """, trackId, yardId, "T-" + trackId.toString().substring(0, 8));
    }

    private void insertTrain(UUID trainId, String number) {
        jdbcTemplate.update("""
                INSERT INTO trains (
                    id, train_number, length_m, service_type, priority, origin, destination
                )
                VALUES (?, ?, 700, 'FREIGHT', 3, 'Origin', 'Destination')
                """, trainId, number);
    }

    private void insertReservation(
            UUID reservationId,
            UUID yardId,
            UUID trackId,
            UUID trainId,
            OffsetDateTime start,
            OffsetDateTime end) {
        jdbcTemplate.update("""
                INSERT INTO track_reservations (
                    id, yard_id, track_id, train_id, operation_type,
                    scheduled_arrival, scheduled_departure, occupied_from, occupied_until,
                    status, created_by, updated_by
                )
                VALUES (?, ?, ?, ?, 'STAGING', ?, ?, ?, ?, 'PLANNED', 'test', 'test')
                """, reservationId, yardId, trackId, trainId, start, end, start, end);
    }
}
