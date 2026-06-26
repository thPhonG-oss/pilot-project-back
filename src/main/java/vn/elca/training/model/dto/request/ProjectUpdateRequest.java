package vn.elca.training.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import vn.elca.training.model.entity.Status;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProjectUpdateRequest {
    @NotBlank(message = "{project.name.required}")
    @Length(max = 50, message = "{project.name.max-length}")
    private String name;

    @NotBlank(message = "{project.customer.required}")
    @Length(max = 50, message = "{project.customer.max-length}")
    private String customer;

    @NotNull(message = "{project.status.required}")
    private Status status;

    @NotNull(message = "{project.startDate.required}")
    private LocalDate startDate;

    private LocalDate endDate;

    private List<String> visas;

    @NotNull(message = "{project.group.required}")
    private Long groupId;

    @NotNull
    private Long version;
}
