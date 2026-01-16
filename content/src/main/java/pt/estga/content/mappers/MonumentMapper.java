package pt.estga.content.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import pt.estga.content.dtos.*;
import pt.estga.content.entities.Monument;
import pt.estga.file.mappers.MediaFileMapper;

@Mapper(componentModel = "spring", uses = {MediaFileMapper.class, pt.estga.administrative.mappers.AdministrativeDivisionMapper.class})
public interface MonumentMapper {

    @Mapping(source = "cover.id", target = "coverId")
    MonumentResponseDto toResponseDto(Monument monument);

    @Mapping(source = "cover.id", target = "coverId")
    MonumentDto toDto(Monument monument);

    @Mapping(source = "cover.id", target = "coverId")
    MonumentListDto toListDto(Monument monument);

    MonumentMapDto toMapDto(Monument monument);

    @Mapping(source = "parishId", target = "parish.id")
    Monument toEntity(MonumentRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "parishId", target = "parish.id")
    void updateEntityFromDto(MonumentRequestDto dto, @MappingTarget Monument entity);

}
