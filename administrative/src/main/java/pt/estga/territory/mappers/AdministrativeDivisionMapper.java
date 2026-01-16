package pt.estga.territory.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.estga.territory.dtos.AdministrativeDivisionDto;
import pt.estga.territory.entities.AdministrativeDivision;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdministrativeDivisionMapper {

    @Mapping(target = "parentId", source = "parent.id")
    AdministrativeDivisionDto toDto(AdministrativeDivision entity);

    List<AdministrativeDivisionDto> toDtoList(List<AdministrativeDivision> entities);

}
