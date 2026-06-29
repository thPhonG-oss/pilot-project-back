package vn.elca.training.model.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import vn.elca.training.service.MessageService;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageService messageService;

    public GlobalExceptionHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<FieldErrorResponse>> handleValidationException(
            MethodArgumentNotValidException ex) {
        return buildValidationErrorResponse(ex.getBindingResult());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse<FieldErrorResponse>> handleBindException(BindException ex) {
        // thrown for @Valid query-param binding (e.g. ProjectSearchCondition), unlike @RequestBody above
        return buildValidationErrorResponse(ex);
    }

    private ResponseEntity<ErrorResponse<FieldErrorResponse>> buildValidationErrorResponse(BindingResult bindingResult) {
        List<FieldErrorResponse> details = bindingResult
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        log.debug("Request validation failed: {}", details);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse<>(null, getMessage("validation.failed"), details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse<FieldErrorResponse>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        List<FieldErrorResponse> details = ex.getConstraintViolations()
                .stream()
                .map(error -> new FieldErrorResponse(
                        error.getPropertyPath().toString(),
                        error.getMessage()))
                .collect(Collectors.toList());

        log.debug("Constraint violation: {}", details);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse<>(null, getMessage("validation.failed"), details));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse<FieldErrorResponse>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        String message = getMessage(errorCode.getMessageKey(), ex.getArgs());

        if (errorCode.getHttpStatus().is5xxServerError()) {
            log.error("Business error {}: {}", errorCode, ex.getMessage(), ex);
        } else {
            log.warn("Business error {}: {}", errorCode, ex.getMessage());
        }

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse<>(
                        errorCode.getCode(),
                        message,
                        buildBusinessErrorDetails(errorCode, message)));
    }

    @ExceptionHandler({ OptimisticLockingFailureException.class })
    public ResponseEntity<ErrorResponse<Object>> handleOptimisticLockException(Exception ex) {
        log.warn("Optimistic lock conflict", ex);
        ErrorCode errorCode = ErrorCode.CONCURRENT_UPDATE;

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse<>(
                        errorCode.getCode(),
                        getMessage(errorCode.getMessageKey()),
                        null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse<FieldErrorResponse>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {
        String constraintName = resolveConstraintName(ex);
        log.warn("Data integrity violation (constraint={}): {}", constraintName,
                ex.getMostSpecificCause().getMessage());

        ErrorCode errorCode = mapConstraintToErrorCode(constraintName);
        String message = getMessage(errorCode.getMessageKey());

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse<>(
                        errorCode.getCode(),
                        message,
                        buildBusinessErrorDetails(errorCode, message)));
    }

    private String resolveConstraintName(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof org.hibernate.exception.ConstraintViolationException) {
            return ((org.hibernate.exception.ConstraintViolationException) cause).getConstraintName();
        }
        return null;
    }

    private ErrorCode mapConstraintToErrorCode(String constraintName) {
        if (constraintName == null) {
            return ErrorCode.DATA_INTEGRITY_VIOLATION;
        }
        String name = constraintName.toLowerCase();
        if (name.contains("uk_project_number")) {
            return ErrorCode.PROJECT_NUMBER_EXISTS;
        }
        return ErrorCode.DATA_INTEGRITY_VIOLATION;
    }

    private List<FieldErrorResponse> buildBusinessErrorDetails(ErrorCode errorCode, String message) {
        if (errorCode.getField() == null || errorCode.getField().trim().isEmpty()) {
            return null;
        }

        List<FieldErrorResponse> details = new ArrayList<>(1);
        details.add(new FieldErrorResponse(errorCode.getField(), message));
        return details;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse<FieldErrorResponse>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        String parameterName = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String detailMessage = getMessage("validation.typeMismatch", parameterName, requiredType);
        List<FieldErrorResponse> details = Collections.singletonList(
                new FieldErrorResponse(parameterName, detailMessage));

        log.debug("Argument type mismatch: {}", detailMessage);
        ErrorCode errorCode = ErrorCode.INVALID_ARGUMENT_TYPE;

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse<>(
                        errorCode.getCode(),
                        getMessage("validation.failed"),
                        details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<Object>> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse<>(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(), getMessage("error.unexpected"),
                        null));
    }

    private String getMessage(String messageCode, Object... args) {
        return messageService.getMessage(messageCode, args);
    }
}
