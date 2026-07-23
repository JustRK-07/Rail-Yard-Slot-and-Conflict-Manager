package com.justrk07.railyard.train;

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
@Table(name = "trains")
public class Train {

    @Id
    private UUID id;

    @Column(name = "train_number", nullable = false, length = 32)
    private String trainNumber;

    @Column(name = "length_m", nullable = false)
    private int lengthMeters;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 24)
    private ServiceType serviceType;

    @Column(name = "priority", nullable = false)
    private short priority;

    @Column(name = "origin", nullable = false, length = 120)
    private String origin;

    @Column(name = "destination", nullable = false, length = 120)
    private String destination;

    @Column(name = "active", nullable = false)
    private boolean active;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "train_required_capabilities",
            joinColumns = @JoinColumn(name = "train_id"))
    @Column(name = "capability", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private Set<TrackCapabilityRef> requiredCapabilities = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Train() {
    }

    private Train(
            UUID id,
            String trainNumber,
            int lengthMeters,
            ServiceType serviceType,
            short priority,
            String origin,
            String destination,
            boolean active,
            Set<TrackCapabilityRef> requiredCapabilities,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.trainNumber = trainNumber;
        this.lengthMeters = lengthMeters;
        this.serviceType = serviceType;
        this.priority = priority;
        this.origin = origin;
        this.destination = destination;
        this.active = active;
        this.requiredCapabilities = requiredCapabilities;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Train create(
            String trainNumber,
            int lengthMeters,
            ServiceType serviceType,
            short priority,
            String origin,
            String destination,
            Set<TrackCapabilityRef> requiredCapabilities) {
        Instant now = Instant.now();
        return new Train(
                UUID.randomUUID(),
                trainNumber,
                lengthMeters,
                serviceType,
                priority,
                origin,
                destination,
                true,
                requiredCapabilities == null ? new HashSet<>() : new HashSet<>(requiredCapabilities),
                now,
                now);
    }

    public void update(
            String trainNumber,
            int lengthMeters,
            ServiceType serviceType,
            short priority,
            String origin,
            String destination,
            boolean active,
            Set<TrackCapabilityRef> requiredCapabilities) {
        this.trainNumber = trainNumber;
        this.lengthMeters = lengthMeters;
        this.serviceType = serviceType;
        this.priority = priority;
        this.origin = origin;
        this.destination = destination;
        this.active = active;
        this.requiredCapabilities = requiredCapabilities == null ? new HashSet<>() : new HashSet<>(requiredCapabilities);
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public int getLengthMeters() {
        return lengthMeters;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public short getPriority() {
        return priority;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public boolean isActive() {
        return active;
    }

    public Set<TrackCapabilityRef> getRequiredCapabilities() {
        return requiredCapabilities;
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
