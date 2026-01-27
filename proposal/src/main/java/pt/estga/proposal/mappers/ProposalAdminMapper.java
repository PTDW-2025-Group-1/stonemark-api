package pt.estga.proposal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pt.estga.proposal.dtos.ActiveDecisionViewDto;
import pt.estga.proposal.dtos.DecisionHistoryItem;
import pt.estga.proposal.dtos.ProposalModeratorViewDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.ProposalDecisionAttempt;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProposalAdminMapper {

    @Mapping(target = "submittedById", source = "submittedBy.id")
    @Mapping(target = "activeDecision", source = "activeDecision")
    ProposalModeratorViewDto toModeratorViewDto(MarkOccurrenceProposal proposal);

    @Mapping(target = "detectedMarkId", source = "detectedMark.id")
    @Mapping(target = "detectedMonumentId", source = "detectedMonument.id")
    ActiveDecisionViewDto toActiveDecisionViewDto(ProposalDecisionAttempt decision);

    List<DecisionHistoryItem> toDecisionHistoryList(List<ProposalDecisionAttempt> decisions);

    DecisionHistoryItem toDecisionHistoryItem(ProposalDecisionAttempt decision);

}
