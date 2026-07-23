package com.justrk07.railyard.track.dto;

import com.justrk07.railyard.track.Track;
import com.justrk07.railyard.track.TrackCapability;
import com.justrk07.railyard.track.TrackPurpose;
import com.justrk07.railyard.track.TrackStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record TrackResponse(
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
        Instant updatedAt
) {
    public static TrackResponse from(Track track) {
        Set<TrackCapability> capabilities = track.getCapabilities().stream()
                .sorted()
                .collect(Collectors.toUnmodifiableSet());
        return new TrackResponse(
                track.getId(),
                track.getYardId(),
                track.getCode(),
                track.getUsableLengthMeters(),
                track.getPurpose(),
                track.getStatus(),
                track.getSetupBufferMinutes(),
                track.getClearanceBufferMinutes(),
                capabilities,
                track.getCreatedAt(),
                track.getUpdatedAt());
    }
}
