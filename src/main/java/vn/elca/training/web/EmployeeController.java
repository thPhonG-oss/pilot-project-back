package vn.elca.training.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.elca.training.model.dto.response.EmployeeDto;
import vn.elca.training.service.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/suggestions")
    public List<EmployeeDto> suggestEmployees(
            @RequestParam(required = false) final String keyword
    ){
        return employeeService.suggestEmployees(keyword);
    }
}
