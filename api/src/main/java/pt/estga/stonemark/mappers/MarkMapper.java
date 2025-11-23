package pt.estga.stonemark.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.estga.stonemark.dtos.content.MarkDto;
import pt.estga.stonemark.entities.content.Mark;

@Mapper(componentModel = "spring")
public interface MarkMapper {
    @Mapping(source = "photo.id", target = "photoId")
    @Mapping(source = "vector.id", target = "vectorId")
    MarkDto markToMarkDto(Mark mark);

    @Mapping(source = "photoId", target = "photo.id")
    @Mapping(source = "vectorId", target = "vector.id")
    Mark markDtoToMark(MarkDto markDto);
}
