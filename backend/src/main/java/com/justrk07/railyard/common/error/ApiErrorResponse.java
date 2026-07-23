package com.justrk07.railyard.common.error;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Stable, RFC-style error envelope returned by every controller when a
 * request cannot be satisfied. Fields are intentionally flat so clients can
 * log them without parsing nested structures.
 */
public record ApiErrorResponse(
        ErrorCode code,
        String message,
        String correlationId,
        Instant timestamp,
        String path,
        List<FieldViolation> fieldErrors,
        Map<String, Object> details
) {

    public static ApiErrorResponse of(
            ErrorCode code,
            String message,
            String correlationId,
            String path,
            List<FieldViolation> fieldErrors,
            Map<String, Object> details) {
        return new ApiErrorResponse(
                code, message, correlationId, Instant.now(), path, fieldErrors, details);
    }

    public record FieldViolation(String field, String message) {
    }
}
