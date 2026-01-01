package pt.estga.content.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import pt.estga.content.dtos.MarkOccurrenceDetailedDto;
import pt.estga.content.dtos.MarkOccurrenceListDto;
import pt.estga.content.entities.MarkOccurrence;
import pt.estga.content.dtos.MarkOccurrenceDto;

@Mapper(componentModel = "spring", uses = {MarkMapper.class, MonumentMapper.class})
public interface MarkOccurrenceMapper {

    @Mapping(target = "coverId", source = "cover.id")
    MarkOccurrenceDto toDto(MarkOccurrence entity);

    @Mapping(target = "coverId", source = "cover.id")
    MarkOccurrenceListDto toListDto(MarkOccurrence entity);

    @Mapping(target = "coverId", source = "cover.id")
    @Mapping(target = "markId", source = "mark.id")
    MarkOccurrenceDetailedDto toDetailedDto(MarkOccurrence entity);

    MarkOccurrence toEntity(MarkOccurrenceDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(MarkOccurrenceDto dto, @MappingTarget MarkOccurrence entity);

}
