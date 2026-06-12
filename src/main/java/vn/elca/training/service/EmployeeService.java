package vn.elca.training.service;

import vn.elca.training.model.entity.Employee;

import java.util.List;

public interface EmployeeService {
    List<Employee> findEmployeesByVisas(List<String> visas);

    Employee findByVisa(String visa);

    List<Employee> suggestEmployees(String keyword);
}
