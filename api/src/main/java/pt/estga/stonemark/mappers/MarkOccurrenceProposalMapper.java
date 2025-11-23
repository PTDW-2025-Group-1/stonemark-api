package pt.estga.stonemark.mappers;

import org.mapstruct.Mapper;
import pt.estga.stonemark.dtos.proposals.MarkOccurrenceProposalDto;
import pt.estga.stonemark.entities.proposals.MarkOccurrenceProposal;

@Mapper(componentModel = "spring", uses = {MediaFileMapper.class, MarkMapper.class, MonumentMapper.class, ProposedMarkMapper.class, ProposedMonumentMapper.class})
public interface MarkOccurrenceProposalMapper {

    MarkOccurrenceProposalDto toDto(MarkOccurrenceProposal entity);

}
