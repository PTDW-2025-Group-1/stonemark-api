package pt.estga.stonemark.mappers;

import org.mapstruct.Mapper;
import pt.estga.stonemark.dtos.content.MarkOccurrenceDto;
import pt.estga.stonemark.entities.content.MarkOccurrence;

@Mapper(componentModel = "spring", uses = {MarkMapper.class, UserMapper.class})
public interface MarkOccurrenceMapper {

    MarkOccurrenceDto toDto(MarkOccurrence entity);

    MarkOccurrence toEntity(MarkOccurrenceDto dto);

}
