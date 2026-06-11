package vn.elca.training.model.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object[] args;

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessageCode());
        this.errorCode = errorCode;
        this.args = args;
    }
}
