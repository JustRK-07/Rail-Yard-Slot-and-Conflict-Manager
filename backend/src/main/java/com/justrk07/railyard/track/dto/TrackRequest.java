package com.justrk07.railyard.track.dto;

import com.justrk07.railyard.track.TrackCapability;
import com.justrk07.railyard.track.TrackPurpose;
import com.justrk07.railyard.track.TrackStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record TrackRequest(
        @NotBlank
        @Size(max = 20)
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "must be uppercase letters, digits, or dashes")
        String code,

        @Min(1)
        int usableLengthMeters,

        @NotNull
        TrackPurpose purpose,

        TrackStatus status,

        @Min(0)
        int setupBufferMinutes,

        @Min(0)
        int clearanceBufferMinutes,

        Set<TrackCapability> capabilities
) {
}
