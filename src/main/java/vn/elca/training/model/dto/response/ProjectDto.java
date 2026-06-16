package vn.elca.training.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.elca.training.model.entity.Status;

import java.time.LocalDate;
import java.util.List;

/**
 * @author gtn
 *
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDto {
    private Long id;

    private Long projectNumber;

    private String name;

    private String customer;

    private Status status;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<EmployeeDto> employeeDtos;
    private GroupDto groupDto;
}
