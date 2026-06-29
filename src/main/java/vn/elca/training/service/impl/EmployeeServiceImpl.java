package vn.elca.training.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.mapper.EmployeeMapper;
import vn.elca.training.model.dto.response.EmployeeDto;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.repository.EmployeeRepository;
import vn.elca.training.service.EmployeeService;
import vn.elca.training.util.LikeEscapeUtil;
import vn.elca.training.util.PaginationUtil;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
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
    public List<EmployeeDto> suggestEmployees(final String keyword){
        if(keyword == null || keyword.trim().isEmpty()) return Collections.emptyList();
        String escapedKeyword = LikeEscapeUtil.escape(keyword.trim());
        return employeeRepository.suggest(escapedKeyword, PaginationUtil.buildDefaultPageSizePagination()).stream()
                .map(employeeMapper::toDto)
                .collect(Collectors.toList());
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
