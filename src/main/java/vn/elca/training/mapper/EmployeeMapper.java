package vn.elca.training.mapper;

import org.mapstruct.Mapper;
import vn.elca.training.model.dto.response.EmployeeDto;
import vn.elca.training.model.entity.Employee;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    EmployeeDto toDto(Employee entity);
}
