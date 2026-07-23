package com.justrk07.railyard.common.error;

/**
 * Thrown by services when a uniqueness constraint would be violated. Mapped
 * to HTTP 409 with the {@link ErrorCode#DUPLICATE_RESOURCE} code.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
