package com.justrk07.railyard.yard.dto;

import com.justrk07.railyard.yard.Yard;
import java.time.Instant;
import java.util.UUID;

public record YardResponse(
        UUID id,
        String code,
        String name,
        String location,
        String timeZone,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static YardResponse from(Yard yard) {
        return new YardResponse(
                yard.getId(),
                yard.getCode(),
                yard.getName(),
                yard.getLocation(),
                yard.getTimeZone(),
                yard.isActive(),
                yard.getCreatedAt(),
                yard.getUpdatedAt());
    }
}
