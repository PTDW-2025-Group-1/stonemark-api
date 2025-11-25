package pt.estga.content.mappers;

import org.mapstruct.Mapper;
import pt.estga.content.dtos.MonumentRequestDto;
import pt.estga.content.dtos.MonumentResponseDto;
import pt.estga.content.entities.Monument;
import pt.estga.content.dtos.MonumentDto;
import pt.estga.user.entities.User;

@Mapper(componentModel = "spring")
public interface MonumentMapper {

    MonumentResponseDto toDto(Monument monument);

    Monument toEntity(MonumentRequestDto dto);

    default String map(User user) {
        if (user == null) return null;
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last = user.getLastName() != null ? user.getLastName() : "";
        return (first + " " + last).trim();
    }

}
