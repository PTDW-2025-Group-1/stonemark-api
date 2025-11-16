package pt.estga.stonemark.mappers;

import org.mapstruct.Mapper;
import pt.estga.stonemark.dtos.content.MarkDto;
import pt.estga.stonemark.entities.content.Mark;

@Mapper(componentModel = "spring", uses = {MediaFileMapper.class})
public interface MarkMapper {
    MarkDto toDto(Mark entity);
}
