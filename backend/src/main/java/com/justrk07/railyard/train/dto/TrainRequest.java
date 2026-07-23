package com.justrk07.railyard.train.dto;

import com.justrk07.railyard.train.ServiceType;
import com.justrk07.railyard.train.TrackCapabilityRef;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record TrainRequest(
        @NotBlank
        @Size(max = 32)
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "must be uppercase letters, digits, or dashes")
        String trainNumber,

        @Min(1)
        int lengthMeters,

        @NotNull
        ServiceType serviceType,

        @Min(1)
        @Max(5)
        short priority,

        @NotBlank
        @Size(max = 120)
        String origin,

        @NotBlank
        @Size(max = 120)
        String destination,

        Boolean active,

        Set<TrackCapabilityRef> requiredCapabilities
) {
}
