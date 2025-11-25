package pt.estga.content.mappers;

import org.mapstruct.Mapper;
import pt.estga.content.entities.Monument;
import pt.estga.content.dtos.MonumentDto;

@Mapper(componentModel = "spring")
public interface MonumentMapper {

    MonumentDto toDto(Monument monument);

    Monument toEntity(MonumentDto dto);

}
