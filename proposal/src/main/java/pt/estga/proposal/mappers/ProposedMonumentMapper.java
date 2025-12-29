package pt.estga.proposal.mappers;

import org.mapstruct.Mapper;
import pt.estga.proposal.dtos.ProposedMonumentDto;
import pt.estga.proposal.entities.ProposedMonument;

@Mapper(componentModel = "spring")
public interface ProposedMonumentMapper {
    ProposedMonumentDto toDto(ProposedMonument entity);
}
