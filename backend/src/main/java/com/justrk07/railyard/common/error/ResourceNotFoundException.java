package com.justrk07.railyard.common.error;

/**
 * Thrown by services when a referenced resource does not exist. Mapped to
 * HTTP 404 with the {@link ErrorCode#RESOURCE_NOT_FOUND} code.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
