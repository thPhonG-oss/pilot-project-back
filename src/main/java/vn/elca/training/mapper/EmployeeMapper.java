package vn.elca.training.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import vn.elca.training.model.dto.response.EmployeeDto;
import vn.elca.training.model.entity.Employee;

@Mapper
public interface EmployeeMapper {
    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    EmployeeDto toDto(Employee entity);
}
