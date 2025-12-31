package pt.estga.proposal.mappers;

import org.mapstruct.Mapper;
import pt.estga.content.mappers.MarkMapper;
import pt.estga.content.mappers.MonumentMapper;
import pt.estga.file.mappers.MediaFileMapper;
import pt.estga.proposal.dtos.MarkOccurrenceProposalDto;
import pt.estga.proposal.dtos.MarkOccurrenceProposalListDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;

@Mapper(componentModel = "spring", uses = {MediaFileMapper.class, MarkMapper.class, MonumentMapper.class})
public interface MarkOccurrenceProposalMapper {

    MarkOccurrenceProposalDto toDto(MarkOccurrenceProposal entity);

    MarkOccurrenceProposalListDto toListDto(MarkOccurrenceProposal entity);

}
