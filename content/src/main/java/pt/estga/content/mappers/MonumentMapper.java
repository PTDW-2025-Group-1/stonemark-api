package pt.estga.content.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import pt.estga.content.dtos.MonumentListDto;
import pt.estga.content.dtos.MonumentMapDto;
import pt.estga.content.dtos.MonumentRequestDto;
import pt.estga.content.dtos.MonumentResponseDto;
import pt.estga.content.entities.Monument;
import pt.estga.file.mappers.MediaFileMapper;
import pt.estga.territory.mappers.AdministrativeDivisionMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MediaFileMapper.class, AdministrativeDivisionMapper.class})
public interface MonumentMapper {

    @Mapping(source = "cover.id", target = "coverId")
    MonumentResponseDto toResponseDto(Monument monument);

    List<MonumentResponseDto> toResponseDto(List<Monument> monuments);

    @Mapping(source = "cover.id", target = "coverId")
    MonumentListDto toListDto(Monument monument);

    List<MonumentListDto> toListDto(List<Monument> monuments);

    MonumentMapDto toMapDto(Monument monument);

    @Mapping(source = "parishId", target = "parish.id")
    @Mapping(source = "municipalityId", target = "municipality.id")
    @Mapping(source = "districtId", target = "district.id")
    Monument toEntity(MonumentRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "parishId", target = "parish.id")
    @Mapping(source = "municipalityId", target = "municipality.id")
    @Mapping(source = "districtId", target = "district.id")
    void updateEntityFromDto(MonumentRequestDto dto, @MappingTarget Monument entity);

}
