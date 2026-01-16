package pt.estga.administrative.mappers;

import org.mapstruct.Mapper;
import pt.estga.administrative.dto.AdministrativeDivisionDto;import pt.estga.administrative.entities.AdministrativeDivision;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdministrativeDivisionMapper {

    AdministrativeDivisionDto toDto(AdministrativeDivision entity);

    List<AdministrativeDivisionDto> toDtoList(List<AdministrativeDivision> entities);

}
