package vn.elca.training.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDeleteRequest {

    @NotEmpty(message = "{project.delete.ids.required}")
    private List<@Min(value = 1L, message = "{project.id.positive}") Long> ids;
}
