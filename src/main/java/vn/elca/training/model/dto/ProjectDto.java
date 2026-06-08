package vn.elca.training.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * @author gtn
 *
 */
public class ProjectDto {
    private Long id;

    @NotBlank(message = "Project name must not be blank")
    private String name;

    @NotNull(message = "Finishing date must not be null")
    private LocalDate finishingDate;

    @NotBlank(message = "Customer name must not be null")
    private String customer;

    public ProjectDto() {}

    public ProjectDto(String name, LocalDate finishingDate, String customer) {
        this.name = name;
        this.finishingDate = finishingDate;
        this.customer = customer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getFinishingDate() {
        return finishingDate;
    }

    public void setFinishingDate(LocalDate finishingDate) {
        this.finishingDate = finishingDate;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }
}
