package pt.estga.proposals.mappers;

import org.mapstruct.Mapper;
import pt.estga.content.mappers.MarkMapper;
import pt.estga.content.mappers.MonumentMapper;
import pt.estga.file.mappers.MediaFileMapper;
import pt.estga.proposals.dtos.MarkOccurrenceProposalDto;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

@Mapper(componentModel = "spring", uses = {MediaFileMapper.class, MarkMapper.class, MonumentMapper.class, ProposedMarkMapper.class, ProposedMonumentMapper.class})
public interface MarkOccurrenceProposalMapper {

    MarkOccurrenceProposalDto toDto(MarkOccurrenceProposal entity);

}
