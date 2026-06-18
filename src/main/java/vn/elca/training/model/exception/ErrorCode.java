package vn.elca.training.model.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    PROJECT_NOT_FOUND( HttpStatus.NOT_FOUND,"1001", "project.notFound"),
    PROJECT_NUMBER_EXISTS( HttpStatus.CONFLICT,"1002", "project.number.exists"),
    INVALID_END_DATE(HttpStatus.BAD_REQUEST, "1003", "project.endDate.invalid"),
    VISAS_NOT_FOUND(HttpStatus.NOT_FOUND, "1004", "project.visas.notFound"),
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "1005", "employee.visa.notFound"),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "1006", "group.id.notFound"),
    PROJECT_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "1007", "project.delete.notAllowed"),
    INVALID_SORT_FIELD(HttpStatus.BAD_REQUEST, "1008", "project.sort.invalid"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "1009", "badRequest"),
    CONCURRENT_UPDATE(HttpStatus.CONFLICT, "1010", "project.concurrentUpdate"),
    INVALID_ARGUMENT_TYPE(HttpStatus.BAD_REQUEST, "1011", "validation.typeMismatch"),
    UNCATEGORIZED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "9999", "error.unexpected"),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String messageKey;
}

