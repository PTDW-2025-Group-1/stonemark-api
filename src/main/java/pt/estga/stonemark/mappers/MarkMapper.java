package pt.estga.stonemark.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.estga.stonemark.dtos.content.MarkDto;
import pt.estga.stonemark.entities.content.Mark;

@Mapper(componentModel = "spring")
public interface MarkMapper {
    @Mapping(source = "cover.id", target = "coverId")
    MarkDto markToMarkDto(Mark mark);

    @Mapping(source = "coverId", target = "cover.id")
    Mark markDtoToMark(MarkDto markDto);
}
