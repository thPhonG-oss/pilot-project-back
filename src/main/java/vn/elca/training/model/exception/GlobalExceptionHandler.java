package vn.elca.training.model.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import vn.elca.training.service.MessageService;

import javax.persistence.OptimisticLockException;
import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageService messageService;

    public GlobalExceptionHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<FieldErrorResponse>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        List<FieldErrorResponse> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        return ResponseEntity.badRequest()
                .body(new ErrorResponse<>(null, getMessage("validation.failed"), details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse<FieldErrorResponse>> handleConstraintViolationException(
            ConstraintViolationException ex
    ) {
        List<FieldErrorResponse> details = ex.getConstraintViolations()
                .stream()
                .map(error -> new FieldErrorResponse(
                        error.getPropertyPath().toString(),
                        error.getMessage()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.badRequest()
                .body(new ErrorResponse<>(null, getMessage("validation.failed"), details));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse<Object>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        if (errorCode.getHttpStatus().is5xxServerError()) {
            log.error("Business error {}: {}", errorCode, ex.getMessage(), ex);
        } else {
            log.warn("Business error {}: {}", errorCode, ex.getMessage());
        }

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse<>(
                        errorCode.getCode(),
                        getMessage(errorCode.getMessageKey(), ex.getArgs()),
                        null
                ));
    }

    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse<Object>> handleOptimisticLockException(Exception ex) {
        log.warn("Optimistic lock conflict", ex);
        ErrorCode errorCode = ErrorCode.CONCURRENT_UPDATE;

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse<>(
                        errorCode.getCode(),
                        getMessage(errorCode.getMessageKey()),
                        null
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse<FieldErrorResponse>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex
    ) {
        String parameterName = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String detailMessage = getMessage("validation.typeMismatch", parameterName, requiredType);
        List<FieldErrorResponse> details = Collections.singletonList(
                new FieldErrorResponse(parameterName, detailMessage)
        );
        ErrorCode errorCode = ErrorCode.INVALID_ARGUMENT_TYPE;

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse<>(
                        errorCode.getCode(),
                        getMessage("validation.failed"),
                        details
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<Object>> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse<>(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(), getMessage("error.unexpected"), null));
    }

    private String getMessage(String messageCode, Object... args) {
        return messageService.getMessage(messageCode, args);
    }
}
