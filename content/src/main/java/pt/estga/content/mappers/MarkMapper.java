package pt.estga.content.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.estga.content.dtos.MarkUpdateDto;
import pt.estga.content.entities.Mark;
import pt.estga.content.dtos.MarkDto;
import pt.estga.file.mappers.MediaFileMapper;

@Mapper(componentModel = "spring", uses = {MediaFileMapper.class})
public interface MarkMapper {

    MarkDto markToMarkDto(Mark mark);

    @Mapping(source = "coverId", target = "cover.id")
    Mark markUpdateDtoToMark(MarkUpdateDto markDto);

}
