package vn.elca.training.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.elca.training.model.entity.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByVisa(String visa);

    List<Employee> findByVisaIn(List<String> visas);

    @Query("SELECT e FROM Employee e " +
            "WHERE lower(e.visa) LIKE lower(concat(:keyword, '%')) ESCAPE '\\' " +
            "OR lower(concat(e.lastName, ' ', e.firstName)) LIKE lower(concat('%', :keyword, '%')) ESCAPE '\\' " +
            "ORDER BY e.visa ASC")
    List<Employee> suggest(@Param("keyword") String keyword, Pageable pageable);
}
