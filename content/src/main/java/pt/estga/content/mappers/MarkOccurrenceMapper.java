package pt.estga.content.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import pt.estga.content.dtos.MarkOccurrenceListDto;
import pt.estga.content.dtos.MarkOccurrenceMapDto;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.dtos.MarkOccurrenceDto;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MarkMapper.class, MonumentMapper.class})
public interface MarkOccurrenceMapper {

    @Mapping(target = "coverId", source = "cover.id")
    @Mapping(target = "markId", source = "mark.id")
    @Mapping(target = "monumentId", source = "monument.id")
    MarkOccurrenceDto toDto(MarkOccurrence entity);

    List<MarkOccurrenceDto> toDto(List<MarkOccurrence> entities);

    MarkOccurrenceListDto toListDto(MarkOccurrence entity);

    MarkOccurrenceMapDto toMapDto(MarkOccurrence entity);

    @Mapping(target = "cover", ignore = true)
    @Mapping(source = "markId", target = "mark.id")
    @Mapping(source = "monumentId", target = "monument.id")
    @Mapping(target = "monument", ignore = true)
    @Mapping(target = "mark", ignore = true)
    MarkOccurrence toEntity(MarkOccurrenceDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cover", ignore = true)
    @Mapping(source = "markId", target = "mark.id")
    @Mapping(source = "monumentId", target = "monument.id")
    @Mapping(target = "monument", ignore = true)
    @Mapping(target = "mark", ignore = true)
    void updateEntityFromDto(MarkOccurrenceDto dto, @MappingTarget MarkOccurrence entity);

}
