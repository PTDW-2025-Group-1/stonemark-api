package pt.estga.content.mappers;

import org.mapstruct.Mapper;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.dtos.MarkOccurrenceDto;

@Mapper(componentModel = "spring", uses = {MarkMapper.class})
public interface MarkOccurrenceMapper {

    MarkOccurrenceDto toDto(MarkOccurrence entity);

    MarkOccurrence toEntity(MarkOccurrenceDto dto);

}
