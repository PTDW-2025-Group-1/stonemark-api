package pt.estga.content.mappers;

import org.mapstruct.Mapper;
import pt.estga.content.dtos.AdministrativeDivisionDto;
import pt.estga.content.entities.AdministrativeDivision;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdministrativeDivisionMapper {

    AdministrativeDivisionDto toDto(AdministrativeDivision entity);

    List<AdministrativeDivisionDto> toDtoList(List<AdministrativeDivision> entities);

}
