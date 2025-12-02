package pt.estga.content.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.estga.content.entities.Mark;
import pt.estga.content.dtos.MarkDto;

@Mapper(componentModel = "spring")
public interface MarkMapper {
    @Mapping(source = "photo.id", target = "photoId")
    MarkDto markToMarkDto(Mark mark);

    @Mapping(source = "photoId", target = "photo.id")
    Mark markDtoToMark(MarkDto markDto);
}
