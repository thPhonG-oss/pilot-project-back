package vn.elca.training.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.elca.training.mapper.EmployeeMapper;
import vn.elca.training.model.dto.response.EmployeeDto;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.service.EmployeeService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;

    public EmployeeController(EmployeeService employeeService, EmployeeMapper employeeMapper) {
        this.employeeService = employeeService;
        this.employeeMapper = employeeMapper;
    }

    @GetMapping("/suggestions")
    public List<EmployeeDto> suggestEmployees(
            @RequestParam(required = false) final String keyword
    ){
        List<Employee> employees = employeeService.suggestEmployees(keyword);

        List<EmployeeDto> employeeDtos  = employees.stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());

        return employeeDtos;
    }
}
