package pt.estga.stonemark.mappers;

import org.mapstruct.Mapper;
import pt.estga.stonemark.dtos.proposals.ProposedMarkDto;
import pt.estga.stonemark.entities.proposals.ProposedMark;

@Mapper(componentModel = "spring", uses = {MediaFileMapper.class})
public interface ProposedMarkMapper {
    ProposedMarkDto toDto(ProposedMark entity);
}
