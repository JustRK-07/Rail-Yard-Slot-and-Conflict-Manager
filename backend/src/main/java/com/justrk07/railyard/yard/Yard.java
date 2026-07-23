package com.justrk07.railyard.yard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "yards")
public class Yard {

    @Id
    private UUID id;

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "location", nullable = false, length = 160)
    private String location;

    @Column(name = "time_zone", nullable = false, length = 80)
    private String timeZone;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Yard() {
    }

    private Yard(
            UUID id,
            String code,
            String name,
            String location,
            String timeZone,
            boolean active,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.location = location;
        this.timeZone = timeZone;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Yard create(
            String code,
            String name,
            String location,
            String timeZone) {
        Instant now = Instant.now();
        return new Yard(UUID.randomUUID(), code, name, location, timeZone, true, now, now);
    }

    public void update(String name, String location, String timeZone, boolean active) {
        this.name = name;
        this.location = location;
        this.timeZone = timeZone;
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public boolean isActive() {
        return active;
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
