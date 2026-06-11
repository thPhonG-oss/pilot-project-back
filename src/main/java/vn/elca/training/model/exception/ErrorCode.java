package vn.elca.training.model.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    PROJECT_NOT_FOUND("1001", "project.notFound"),
    PROJECT_NUMBER_EXISTS("1002", "project.number.exists"),
    INVALID_END_DATE("1003", "project.endDate.invalid"),
    VISAS_NOT_FOUND("1004", "project.visas.notFound");

    private final String code;
    private final String messageCode;
}
