package pt.estga.content.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.estga.content.dtos.MonumentDto;
import pt.estga.content.dtos.MonumentRequestDto;
import pt.estga.content.dtos.MonumentResponseDto;
import pt.estga.content.entities.Monument;
import pt.estga.file.mappers.MediaFileMapper;

@Mapper(componentModel = "spring", uses = {MediaFileMapper.class})
public interface MonumentMapper {

    @Mapping(source = "cover.id", target = "coverId")
    MonumentResponseDto toResponseDto(Monument monument);

    @Mapping(source = "cover.id", target = "coverId")
    MonumentDto toDto(Monument monument);

    Monument toEntity(MonumentRequestDto dto);

}
