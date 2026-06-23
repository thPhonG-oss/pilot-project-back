package vn.elca.training.web;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.elca.training.model.dto.response.EmployeeDto;
import vn.elca.training.service.EmployeeService;

import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@Validated
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/suggestions")
    public List<EmployeeDto> suggestEmployees(
            @RequestParam(required = false)
            @Size(max = 50, message = "{search.keyword.max-length}")
            final String keyword
    ){
        return employeeService.suggestEmployees(keyword);
    }
}
