package vn.elca.training.model.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "1000", "validation.failed", null),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "1001", "project.notFound", null),
    PROJECT_NUMBER_EXISTS(HttpStatus.CONFLICT, "1002", "project.number.exists", "projectNumber"),
    INVALID_END_DATE(HttpStatus.BAD_REQUEST, "1003", "project.endDate.invalid", "endDate"),
    VISAS_NOT_FOUND(HttpStatus.NOT_FOUND, "1004", "project.visas.notFound", "visas"),
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "1005", "employee.visa.notFound", null),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "1006", "group.id.notFound", "groupId"),
    PROJECT_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "1007", "project.delete.notAllowed", null),
    INVALID_SORT_FIELD(HttpStatus.BAD_REQUEST, "1008", "project.sort.invalid", null),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "1009", "badRequest", null),
    CONCURRENT_UPDATE(HttpStatus.CONFLICT, "1010", "project.concurrentUpdate", null),
    INVALID_ARGUMENT_TYPE(HttpStatus.BAD_REQUEST, "1011", "validation.typeMismatch", null),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "1012", "data.integrity.violation", null),
    INVALID_SEARCH_START_DATE_RANGE(HttpStatus.BAD_REQUEST, "1013", "search.startDate.range.invalid", "startDateTo"),
    INVALID_SEARCH_END_DATE_RANGE(HttpStatus.BAD_REQUEST, "1014", "search.endDate.range.invalid", "endDateTo"),
    UNCATEGORIZED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "9999", "error.unexpected", null),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String messageKey;
    /**
     * Optional form field this error primarily relates to.
     * When present, the API will include a FieldErrorResponse in "details" so the frontend can highlight the input.
     */
    private final String field;
}

