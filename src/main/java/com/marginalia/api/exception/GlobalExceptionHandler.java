package com.marginalia.api.exception;

import com.marginalia.api.dto.ApiErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/** Converts controller and service exceptions into the API's consistent JSON error representation. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Converts request-body validation failures into a bad-request response.
     *
     * @param exception validation exception containing binding errors
     * @param request failed HTTP request
     * @return consistent bad-request error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getAllErrors().stream()
                .map(error -> error instanceof FieldError fieldError
                        ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                        : error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        return response(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Converts method-level constraint violations into a bad-request response.
     *
     * @param exception validation exception containing constraint violations
     * @param request failed HTTP request
     * @return consistent bad-request error response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));

        return response(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Converts missing entities into a not-found response.
     *
     * @param exception missing-entity exception
     * @param request failed HTTP request
     * @return consistent not-found error response
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(
            EntityNotFoundException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    /**
     * Converts authorization failures into a forbidden response.
     *
     * @param exception access-denied exception
     * @param request failed HTTP request
     * @return consistent forbidden error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.FORBIDDEN, exception.getMessage(), request);
    }

    /**
     * Converts image-provider failures into a controlled bad-gateway response.
     *
     * @param exception failed Cloudinary upload
     * @param request failed HTTP request
     * @return consistent bad-gateway error response
     */
    @ExceptionHandler(CloudinaryUploadException.class)
    public ResponseEntity<ApiErrorResponse> handleCloudinaryUpload(
            CloudinaryUploadException exception,
            HttpServletRequest request
    ) {
        log.error("Cloudinary image upload failed", exception);
        return response(HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
    }

    /**
     * Converts remaining exceptions according to Spring error metadata or a safe internal-error fallback.
     *
     * @param exception unhandled exception
     * @param request failed HTTP request
     * @return consistent error response with the resolved HTTP status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        HttpStatus status = resolveStatus(exception);
        if (status.is5xxServerError()) {
            log.error("Unhandled request error", exception);
        }

        String message = status.is5xxServerError()
                ? "An unexpected error occurred"
                : exception.getMessage();
        return response(status, message, request);
    }

    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        String resolvedMessage = message == null || message.isBlank()
                ? status.getReasonPhrase()
                : message;
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                resolvedMessage,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    private HttpStatus resolveStatus(Exception exception) {
        if (exception instanceof ErrorResponse errorResponse) {
            return HttpStatus.valueOf(errorResponse.getStatusCode().value());
        }

        ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(
                exception.getClass(),
                ResponseStatus.class
        );
        return responseStatus == null
                ? HttpStatus.INTERNAL_SERVER_ERROR
                : responseStatus.code();
    }
}
