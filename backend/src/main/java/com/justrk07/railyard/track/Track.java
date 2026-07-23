package com.justrk07.railyard.track;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tracks")
public class Track {

    @Id
    private UUID id;

    @Column(name = "yard_id", nullable = false)
    private UUID yardId;

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "usable_length_m", nullable = false)
    private int usableLengthMeters;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 24)
    private TrackPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private TrackStatus status;

    @Column(name = "setup_buffer_minutes", nullable = false)
    private int setupBufferMinutes;

    @Column(name = "clearance_buffer_minutes", nullable = false)
    private int clearanceBufferMinutes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "track_capabilities",
            joinColumns = @JoinColumn(name = "track_id"))
    @Column(name = "capability", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private Set<TrackCapability> capabilities = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Track() {
    }

    private Track(
            UUID id,
            UUID yardId,
            String code,
            int usableLengthMeters,
            TrackPurpose purpose,
            TrackStatus status,
            int setupBufferMinutes,
            int clearanceBufferMinutes,
            Set<TrackCapability> capabilities,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.yardId = yardId;
        this.code = code;
        this.usableLengthMeters = usableLengthMeters;
        this.purpose = purpose;
        this.status = status;
        this.setupBufferMinutes = setupBufferMinutes;
        this.clearanceBufferMinutes = clearanceBufferMinutes;
        this.capabilities = capabilities;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Track create(
            UUID yardId,
            String code,
            int usableLengthMeters,
            TrackPurpose purpose,
            TrackStatus status,
            int setupBufferMinutes,
            int clearanceBufferMinutes,
            Set<TrackCapability> capabilities) {
        Instant now = Instant.now();
        return new Track(
                UUID.randomUUID(),
                yardId,
                code,
                usableLengthMeters,
                purpose,
                status == null ? TrackStatus.OPERATIONAL : status,
                setupBufferMinutes,
                clearanceBufferMinutes,
                capabilities == null ? new HashSet<>() : new HashSet<>(capabilities),
                now,
                now);
    }

    public void update(
            String code,
            int usableLengthMeters,
            TrackPurpose purpose,
            TrackStatus status,
            int setupBufferMinutes,
            int clearanceBufferMinutes,
            Set<TrackCapability> capabilities) {
        this.code = code;
        this.usableLengthMeters = usableLengthMeters;
        this.purpose = purpose;
        this.status = status;
        this.setupBufferMinutes = setupBufferMinutes;
        this.clearanceBufferMinutes = clearanceBufferMinutes;
        this.capabilities = capabilities == null ? new HashSet<>() : new HashSet<>(capabilities);
        this.updatedAt = Instant.now();
    }

    public void changeStatus(TrackStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getYardId() {
        return yardId;
    }

    public String getCode() {
        return code;
    }

    public int getUsableLengthMeters() {
        return usableLengthMeters;
    }

    public TrackPurpose getPurpose() {
        return purpose;
    }

    public TrackStatus getStatus() {
        return status;
    }

    public int getSetupBufferMinutes() {
        return setupBufferMinutes;
    }

    public int getClearanceBufferMinutes() {
        return clearanceBufferMinutes;
    }

    public Set<TrackCapability> getCapabilities() {
        return capabilities;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
