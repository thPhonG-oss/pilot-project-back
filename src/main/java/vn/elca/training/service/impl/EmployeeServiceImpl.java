package vn.elca.training.service.impl;

import org.springframework.stereotype.Service;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.repository.EmployeeRepository;
import vn.elca.training.service.EmployeeService;
import vn.elca.training.util.PaginationUtil;

import java.util.ArrayList;
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
    public List<Employee> findEmployeesByVisas(final List<String> visas){
        List<String> normalizedVisas = normalizedVisas(visas);

        if(normalizedVisas.isEmpty()) {
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
    public Employee findByVisa(final String visa){
        return employeeRepository.findByVisa(visa).orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    @Override
    public List<Employee> suggestEmployees(final String keyword){
        List<Employee> employees = new ArrayList<>();
        if(keyword == null || keyword.trim().isEmpty()) return employees;
        return employeeRepository.suggest(keyword.trim(), PaginationUtil.buildDefaultPageSizePagination());
    }

    private List<String> normalizedVisas(final List<String> visas){
        if(visas == null || visas.isEmpty()) return Collections.emptyList();

        List<String> normalizedVisas = visas.stream()
                .filter(visa -> visa != null && !visa.trim().isEmpty())
                .map(visa -> visa.trim().toUpperCase())
                .distinct()
                .collect(Collectors.toList());

        if (normalizedVisas.isEmpty()) {
            return Collections.emptyList();
        }

        return  normalizedVisas;
    }
}
