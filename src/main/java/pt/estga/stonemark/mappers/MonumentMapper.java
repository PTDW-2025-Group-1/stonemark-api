package pt.estga.stonemark.mappers;

import org.mapstruct.Mapper;
import pt.estga.stonemark.dtos.content.MonumentDto;
import pt.estga.stonemark.entities.content.Monument;

@Mapper(componentModel = "spring")
public interface MonumentMapper {

    MonumentDto toDto(Monument monument);

    Monument toEntity(MonumentDto dto);
}
