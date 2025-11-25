package pt.estga.shared.mappers;

import org.mapstruct.Mapper;
import pt.estga.stonemark.dtos.proposals.ProposedMonumentDto;
import pt.estga.stonemark.entities.proposals.ProposedMonument;

@Mapper(componentModel = "spring")
public interface ProposedMonumentMapper {
    ProposedMonumentDto toDto(ProposedMonument entity);
}
