package vn.elca.training.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.elca.training.model.entity.Status;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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

    @NotBlank(message = "Project name must not be blank")
    private String name;

    @NotBlank(message = "Customer name must not be null")
    private String customer;

    private Status status;

    private LocalDate startDate;

    private LocalDate endDate;
}
