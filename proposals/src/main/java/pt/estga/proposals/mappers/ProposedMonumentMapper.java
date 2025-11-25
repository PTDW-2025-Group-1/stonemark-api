package pt.estga.proposals.mappers;

import org.mapstruct.Mapper;
import pt.estga.proposals.dtos.ProposedMonumentDto;
import pt.estga.proposals.entities.ProposedMonument;

@Mapper(componentModel = "spring")
public interface ProposedMonumentMapper {
    ProposedMonumentDto toDto(ProposedMonument entity);
}
