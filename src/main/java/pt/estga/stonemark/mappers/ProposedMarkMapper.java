package pt.estga.stonemark.mappers;

import org.mapstruct.Mapper;
import pt.estga.stonemark.dtos.proposals.ProposedMarkDto;
import pt.estga.stonemark.entities.proposals.ProposedMark;

@Mapper(componentModel = "spring")
public interface ProposedMarkMapper {

    ProposedMarkDto toDto(ProposedMark entity);

}
