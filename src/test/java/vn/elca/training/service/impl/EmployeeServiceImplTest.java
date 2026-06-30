package vn.elca.training.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import vn.elca.training.mapper.EmployeeMapper;
import vn.elca.training.model.dto.response.EmployeeDto;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.ErrorCode;
import vn.elca.training.repository.EmployeeRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeServiceImpl Unit Tests")
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employeeQmv;
    private Employee employeeHnh;
    private EmployeeDto employeeQmvDto;

    @BeforeEach
    void setUp() {
        employeeQmv = new Employee("QMV", "Quy", "Van", LocalDate.of(1990, 2, 3));
        ReflectionTestUtils.setField(employeeQmv, "id", 1L);

        employeeHnh = new Employee("HNH", "Hanh", "Ho", LocalDate.of(1992, 5, 12));
        ReflectionTestUtils.setField(employeeHnh, "id", 2L);

        employeeQmvDto = new EmployeeDto(1L, 1L, "QMV", "Quy", "Van");
    }

    @Test
    @DisplayName("Should return employees when all visas exist")
    void findEmployeesByVisas_shouldReturnEmployees_whenAllVisasExist() {
        // Arrange
        when(employeeRepository.findByVisaIn(Arrays.asList("QMV", "HNH")))
                .thenReturn(Arrays.asList(employeeQmv, employeeHnh));

        // Act
        List<Employee> result = employeeService.findEmployeesByVisas(Arrays.asList("QMV", "HNH"));

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Employee::getVisa).containsExactlyInAnyOrder("QMV", "HNH");

        ArgumentCaptor<List<String>> visasCaptor = ArgumentCaptor.forClass(List.class);
        verify(employeeRepository).findByVisaIn(visasCaptor.capture());
        assertThat(visasCaptor.getValue()).containsExactly("QMV", "HNH");
    }

    @Test
    @DisplayName("Should normalize visas to uppercase before lookup")
    void findEmployeesByVisas_shouldNormalizeVisas_whenMixedCaseProvided() {
        // Arrange
        when(employeeRepository.findByVisaIn(Collections.singletonList("QMV")))
                .thenReturn(Collections.singletonList(employeeQmv));

        // Act
        List<Employee> result = employeeService.findEmployeesByVisas(Collections.singletonList("qmv"));

        // Assert
        assertThat(result).containsExactly(employeeQmv);
        verify(employeeRepository).findByVisaIn(Collections.singletonList("QMV"));
    }

    @Test
    @DisplayName("Should return empty list when visas is null")
    void findEmployeesByVisas_shouldReturnEmptyList_whenVisasIsNull() {
        // Act
        List<Employee> result = employeeService.findEmployeesByVisas(null);

        // Assert
        assertThat(result).isEmpty();
        verify(employeeRepository, never()).findByVisaIn(any());
    }

    @Test
    @DisplayName("Should return empty list when visas is empty")
    void findEmployeesByVisas_shouldReturnEmptyList_whenVisasIsEmpty() {
        // Act
        List<Employee> result = employeeService.findEmployeesByVisas(Collections.emptyList());

        // Assert
        assertThat(result).isEmpty();
        verify(employeeRepository, never()).findByVisaIn(any());
    }

    @Test
    @DisplayName("Should return empty list when visas contain only blank values")
    void findEmployeesByVisas_shouldReturnEmptyList_whenVisasAreBlank() {
        // Act
        List<Employee> result = employeeService.findEmployeesByVisas(Arrays.asList("  ", null, ""));

        // Assert
        assertThat(result).isEmpty();
        verify(employeeRepository, never()).findByVisaIn(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when some visas do not exist")
    void findEmployeesByVisas_shouldThrowBusinessException_whenVisasMissing() {
        // Arrange
        when(employeeRepository.findByVisaIn(Arrays.asList("QMV", "XXX")))
                .thenReturn(Collections.singletonList(employeeQmv));

        // Act & Assert
        assertThatThrownBy(() -> employeeService.findEmployeesByVisas(Arrays.asList("QMV", "XXX")))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException businessException = (BusinessException) ex;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.VISAS_NOT_FOUND);
                    assertThat(businessException.getArgs()).containsExactly("XXX");
                });

        verify(employeeRepository).findByVisaIn(Arrays.asList("QMV", "XXX"));
    }

    @Test
    @DisplayName("Should return employee when visa exists")
    void findByVisa_shouldReturnEmployee_whenVisaExists() {
        // Arrange
        when(employeeRepository.findByVisa("QMV")).thenReturn(Optional.of(employeeQmv));

        // Act
        Employee result = employeeService.findByVisa("QMV");

        // Assert
        assertThat(result).isEqualTo(employeeQmv);
        verify(employeeRepository).findByVisa("QMV");
    }

    @Test
    @DisplayName("Should throw BusinessException when visa does not exist")
    void findByVisa_shouldThrowBusinessException_whenVisaNotFound() {
        // Arrange
        when(employeeRepository.findByVisa("ZZZ")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.findByVisa("ZZZ"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));

        verify(employeeRepository).findByVisa("ZZZ");
    }

    @Test
    @DisplayName("Should return mapped employee DTOs when keyword matches")
    void suggestEmployees_shouldReturnMappedDtos_whenKeywordProvided() {
        // Arrange
        when(employeeRepository.suggest(eq("qm"), any(Pageable.class)))
                .thenReturn(Collections.singletonList(employeeQmv));
        when(employeeMapper.toDto(employeeQmv)).thenReturn(employeeQmvDto);

        // Act
        List<EmployeeDto> result = employeeService.suggestEmployees("  qm  ");

        // Assert
        assertThat(result).containsExactly(employeeQmvDto);
        verify(employeeRepository).suggest(eq("qm"), any(Pageable.class));
        verify(employeeMapper).toDto(employeeQmv);
    }

    @Test
    @DisplayName("Should escape LIKE wildcards before repository lookup")
    void suggestEmployees_shouldEscapeWildcards_beforeRepositoryLookup() {
        when(employeeRepository.suggest(eq("a\\%b"), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        employeeService.suggestEmployees("a%b");

        verify(employeeRepository).suggest(eq("a\\%b"), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return empty list when keyword is null")
    void suggestEmployees_shouldReturnEmptyList_whenKeywordIsNull() {
        // Act
        List<EmployeeDto> result = employeeService.suggestEmployees(null);

        // Assert
        assertThat(result).isEmpty();
        verify(employeeRepository, never()).suggest(anyString(), any(Pageable.class));
        verify(employeeMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should return empty list when keyword is blank")
    void suggestEmployees_shouldReturnEmptyList_whenKeywordIsBlank() {
        // Act
        List<EmployeeDto> result = employeeService.suggestEmployees("   ");

        // Assert
        assertThat(result).isEmpty();
        verify(employeeRepository, never()).suggest(anyString(), any(Pageable.class));
        verify(employeeMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Should return empty list when repository returns no matches")
    void suggestEmployees_shouldReturnEmptyList_whenNoMatchesFound() {
        // Arrange
        when(employeeRepository.suggest(eq("zzz"), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<EmployeeDto> result = employeeService.suggestEmployees("zzz");

        // Assert
        assertThat(result).isEmpty();
        verify(employeeRepository).suggest(eq("zzz"), any(Pageable.class));
        verify(employeeMapper, never()).toDto(any());
    }
}
