package com.justrk07.railyard.common.error;

/**
 * Thrown when a request is structurally valid but violates a documented
 * business rule. Mapped to HTTP 422 with the
 * {@link ErrorCode#BUSINESS_RULE_VIOLATION} code.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
