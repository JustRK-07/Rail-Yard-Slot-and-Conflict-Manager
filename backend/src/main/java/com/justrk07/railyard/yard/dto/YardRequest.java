package com.justrk07.railyard.yard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record YardRequest(
        @NotBlank
        @Size(max = 20)
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "must be uppercase letters, digits, or dashes")
        String code,

        @NotBlank
        @Size(max = 120)
        String name,

        @NotBlank
        @Size(max = 160)
        String location,

        @NotBlank
        @Size(max = 80)
        String timeZone,

        Boolean active
) {
}
