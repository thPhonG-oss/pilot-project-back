package vn.elca.training.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import vn.elca.training.model.entity.Status;

import javax.validation.constraints.Size;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProjectSearchCondition {

    @Size(max = 50, message = "{search.keyword.max-length}")
    private String keyword;

    private Status status;

    @Size(max = 3, message = "{search.visa.max-length}")
    private String leaderVisa;

    @Size(max = 3, message = "{search.visa.max-length}")
    private String memberVisa;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDateTo;
}
