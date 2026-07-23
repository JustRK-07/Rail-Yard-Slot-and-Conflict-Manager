package com.justrk07.railyard.common.error;

/**
 * Stable, machine-readable identifiers returned in the {@code code} field of
 * every API error response. New codes are appended; existing codes are never
 * renamed or removed so client code can rely on them.
 */
public enum ErrorCode {
    VALIDATION_FAILED,
    RESOURCE_NOT_FOUND,
    DUPLICATE_RESOURCE,
    BUSINESS_RULE_VIOLATION,
    TRACK_RESERVATION_CONFLICT,
    INTERNAL_ERROR
}
