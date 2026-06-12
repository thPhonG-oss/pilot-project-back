package vn.elca.training.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class EmployeeDto {
    private Long id;
    private String visa;
    private String firstName;
    private String lastName;
}
