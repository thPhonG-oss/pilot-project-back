package vn.elca.training.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.elca.training.model.entity.Status;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * @author gtn
 *
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProjectDto {
    private Long id;

    private Long projectNumber;

    private String name;

    private String customer;

    private Status status;

    private LocalDate startDate;

    private LocalDate endDate;
}
