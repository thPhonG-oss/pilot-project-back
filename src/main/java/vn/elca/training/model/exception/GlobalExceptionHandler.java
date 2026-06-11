package vn.elca.training.model.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.elca.training.service.MessageService;

import javax.validation.ConstraintViolationException;
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
        HttpStatus status = ErrorCode.PROJECT_NOT_FOUND.equals(errorCode)
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(new ErrorResponse<>(
                        errorCode.getCode(),
                        getMessage(errorCode.getMessageCode(), ex.getArgs()),
                        null
                ));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse<Object>> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse<>(null, ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<Object>> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse<>(null, getMessage("error.unexpected"), null));
    }

    private String getMessage(String messageCode, Object... args) {
        return messageService.getMessage(messageCode, args);
    }
}
