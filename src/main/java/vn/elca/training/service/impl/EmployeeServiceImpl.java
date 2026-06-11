package vn.elca.training.service.impl;

import org.springframework.stereotype.Service;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.repository.EmployeeRepository;
import vn.elca.training.service.EmployeeService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<Employee> findEmployeesByVisas(List<String> visas){
        if (visas == null || visas.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> normalizedVisas = visas.stream()
                .filter(visa -> visa != null && !visa.trim().isEmpty())
                .map(visa -> visa.trim().toUpperCase())
                .distinct()
                .collect(Collectors.toList());

        if (normalizedVisas.isEmpty()) {
            return Collections.emptyList();
        }

        List<Employee> employees = employeeRepository.findByVisaIn(normalizedVisas);
        Set<String> existingVisas = employees.stream()
                .map(employee -> employee.getVisa().toUpperCase())
                .collect(Collectors.toSet());

        List<String> missingVisas = normalizedVisas.stream()
                .filter(visa -> !existingVisas.contains(visa))
                .collect(Collectors.toList());

        if (!missingVisas.isEmpty()) {
            throw new BusinessException(ErrorCode.VISAS_NOT_FOUND, String.join(", ", missingVisas));
        }

        return employees;
    }

    @Override
    public Employee findByVisa(String visa){
        return employeeRepository.findByVisa(visa).orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }
}
