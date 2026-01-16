package pt.estga.content.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import pt.estga.content.dtos.MarkOccurrenceDto;
import pt.estga.content.entities.MarkOccurrence;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MarkMapper.class, MonumentMapper.class})
public interface MarkOccurrenceMapper {

    @Mapping(target = "coverId", source = "cover.id")
    @Mapping(target = "markId", source = "mark.id")
    MarkOccurrenceDto toDto(MarkOccurrence entity);

    List<MarkOccurrenceDto> toDto(List<MarkOccurrence> entities);

    MarkOccurrence toEntity(MarkOccurrenceDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(MarkOccurrenceDto dto, @MappingTarget MarkOccurrence entity);

}
