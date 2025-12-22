package pt.estga.content.mappers;

import org.mapstruct.Mapper;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.dtos.MarkOccurrenceDto;
import pt.estga.user.entities.User;

@Mapper(componentModel = "spring", uses = {MarkMapper.class, MonumentMapper.class})
public interface MarkOccurrenceMapper {

    MarkOccurrenceDto toDto(MarkOccurrence entity);

    MarkOccurrence toEntity(MarkOccurrenceDto dto);

    default String map(User user) {
        if (user == null) return null;
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last = user.getLastName() != null ? user.getLastName() : "";
        return (first + " " + last).trim();
    }

}
