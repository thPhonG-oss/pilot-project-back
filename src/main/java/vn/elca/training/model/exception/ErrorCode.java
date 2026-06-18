package vn.elca.training.model.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    PROJECT_NOT_FOUND("1001", "project.notFound"),
    PROJECT_NUMBER_EXISTS("1002", "project.number.exists"),
    INVALID_END_DATE("1003", "project.endDate.invalid"),
    VISAS_NOT_FOUND("1004", "project.visas.notFound"),
    EMPLOYEE_NOT_FOUND("1005", "employee.visa.notFound"),
    GROUP_NOT_FOUND("1006", "group.id.notFound"),
    INVALID_NEW_PROJECT_STATUS("1007", "project.create.invalidStatus"),
    PROJECT_NUMBER_NOT_CHANGE("1008", "project.update.projectNumber"),
    STATUS_NOT_ROLLBACK("1009", "project.update.statusNotRollback"),
    PROJECT_DELETE_NOT_ALLOWED("1010", "project.delete.notAllowed")

    ;

    private final String code;
    private final String messageCode;
}

