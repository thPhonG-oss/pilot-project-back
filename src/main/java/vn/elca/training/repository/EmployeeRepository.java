package vn.elca.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.elca.training.model.entity.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByVisa(String visa);

    List<Employee> findByVisaIn(List<String> visas);
}
