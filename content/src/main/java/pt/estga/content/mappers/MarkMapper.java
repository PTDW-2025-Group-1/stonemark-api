package pt.estga.content.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pt.estga.content.dtos.MarkListDto;
import pt.estga.content.dtos.MarkUpdateDto;
import pt.estga.content.entities.Mark;
import pt.estga.content.dtos.MarkDto;
import pt.estga.file.mappers.MediaFileMapper;

@Mapper(componentModel = "spring", uses = {MediaFileMapper.class})
public interface MarkMapper {

    @Mapping(source = "cover.id", target = "coverId")
    MarkDto toDto(Mark mark);

    @Mapping(source = "cover.id", target = "coverId")
    MarkListDto toListDto(Mark mark);

    @Mapping(source = "coverId", target = "cover.id")
    Mark updateDtoToEntity(MarkUpdateDto markDto);

    @Mapping(source = "coverId", target = "cover.id")
    void updateEntityFromDto(MarkUpdateDto dto, @MappingTarget Mark entity);

}
