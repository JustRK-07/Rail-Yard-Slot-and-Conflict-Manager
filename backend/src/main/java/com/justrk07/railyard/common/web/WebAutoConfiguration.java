package com.justrk07.railyard.common.web;

import com.justrk07.railyard.common.error.ApiErrorResponse;
import com.justrk07.railyard.common.error.BusinessRuleException;
import com.justrk07.railyard.common.error.DuplicateResourceException;
import com.justrk07.railyard.common.error.ErrorCode;
import com.justrk07.railyard.common.error.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Translates application and validation exceptions into the project's
 * {@link ApiErrorResponse} envelope. The mapping table is intentionally
 * small; if a new exception type needs handling, add it here rather than
 * scattering catch blocks across services.
 */
@RestControllerAdvice
public class WebAutoConfiguration {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return body(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), request, null, null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {
        return body(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_RESOURCE, ex.getMessage(), request, null, null);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(
            BusinessRuleException ex, HttpServletRequest request) {
        return body(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCode.BUSINESS_RULE_VIOLATION, ex.getMessage(), request, null, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleBeanValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiErrorResponse.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiErrorResponse.FieldViolation(
                        error.getField(),
                        error.getDefaultMessage() == null ? "invalid" : error.getDefaultMessage()))
                .toList();
        return body(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                "Request validation failed",
                request,
                violations,
                null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiErrorResponse.FieldViolation> violations = ex.getConstraintViolations().stream()
                .map(violation -> new ApiErrorResponse.FieldViolation(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .toList();
        return body(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                "Request validation failed",
                request,
                violations,
                null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return body(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                "Request body could not be parsed",
                request,
                null,
                null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return body(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                ex.getMessage() == null ? "Invalid argument" : ex.getMessage(),
                request,
                null,
                null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String expected = ex.getRequiredType() == null ? "valid value" : ex.getRequiredType().getSimpleName();
        String message = String.format(
                "Parameter '%s' must be a %s",
                ex.getName(),
                expected);
        return body(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_FAILED,
                message,
                request,
                null,
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return body(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                "Unexpected server error",
                request,
                null,
                Map.of("exception", ex.getClass().getSimpleName()));
    }

    private ResponseEntity<ApiErrorResponse> body(
            HttpStatus status,
            ErrorCode code,
            String message,
            HttpServletRequest request,
            List<ApiErrorResponse.FieldViolation> fieldErrors,
            Map<String, Object> details) {
        ApiErrorResponse response = ApiErrorResponse.of(
                code,
                message,
                MDC.get(com.justrk07.railyard.common.web.CorrelationIdFilter.MDC_KEY),
                request.getRequestURI(),
                fieldErrors,
                details);
        return ResponseEntity.status(status).body(response);
    }
}
