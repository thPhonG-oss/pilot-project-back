package vn.elca.training.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProjectCreationRequest {

    @NotNull(message = "{project.number.required}")
    @Min(value = 1L, message = "{project.number.positive}")
    private Long projectNumber;

    @NotBlank(message = "{project.name.required}")
    @Length(max = 50, message = "{project.name.max-length}")
    private String name;

    @NotBlank(message = "{project.customer.required}")
    @Length(max = 50, message = "{project.customer.max-length}")
    private String customer;

    @NotNull(message = "{project.startDate.required}")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "{project.visa.required}")
    private List<String> visas;

    @NotNull(message = "{project.group.required}")
    private Long groupId;
}
