package vn.elca.training.model.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class FieldErrorResponse {
    private String field;
    private String message;
}
