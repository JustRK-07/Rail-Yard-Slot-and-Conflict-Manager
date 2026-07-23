package com.justrk07.railyard.train.dto;

import com.justrk07.railyard.train.ServiceType;
import com.justrk07.railyard.train.TrackCapabilityRef;
import com.justrk07.railyard.train.Train;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record TrainResponse(
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
        Instant updatedAt
) {
    public static TrainResponse from(Train train) {
        Set<TrackCapabilityRef> capabilities = train.getRequiredCapabilities().stream()
                .sorted()
                .collect(Collectors.toUnmodifiableSet());
        return new TrainResponse(
                train.getId(),
                train.getTrainNumber(),
                train.getLengthMeters(),
                train.getServiceType(),
                train.getPriority(),
                train.getOrigin(),
                train.getDestination(),
                train.isActive(),
                capabilities,
                train.getCreatedAt(),
                train.getUpdatedAt());
    }
}
