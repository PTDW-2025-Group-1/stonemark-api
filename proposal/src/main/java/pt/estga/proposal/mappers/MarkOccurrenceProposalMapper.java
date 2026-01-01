package pt.estga.proposal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.estga.content.mappers.MarkMapper;
import pt.estga.content.mappers.MonumentMapper;
import pt.estga.proposal.dtos.MarkOccurrenceProposalDto;
import pt.estga.proposal.dtos.MarkOccurrenceProposalListDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;

@Mapper(componentModel = "spring", uses = {MarkMapper.class, MonumentMapper.class})
public interface MarkOccurrenceProposalMapper {

    @Mapping(source = "originalMediaFile.id", target = "coverId")
    MarkOccurrenceProposalDto toDto(MarkOccurrenceProposal entity);

    @Mapping(source = "originalMediaFile.id", target = "coverId")
    MarkOccurrenceProposalListDto toListDto(MarkOccurrenceProposal entity);

}
