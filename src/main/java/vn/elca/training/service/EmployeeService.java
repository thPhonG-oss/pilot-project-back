package vn.elca.training.service;

import vn.elca.training.model.dto.response.EmployeeDto;
import vn.elca.training.model.entity.Employee;

import java.util.List;

public interface EmployeeService {
    List<Employee> findEmployeesByVisas(List<String> visas);

    Employee findByVisa(String visa);

    List<EmployeeDto> suggestEmployees(String keyword);
}
