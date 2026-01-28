package pt.estga.decision.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pt.estga.decision.dtos.ActiveDecisionViewDto;
import pt.estga.decision.dtos.DecisionHistoryItem;
import pt.estga.decision.dtos.ProposalAdminListDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.decision.entities.ProposalDecisionAttempt;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProposalAdminMapper {

    @Mapping(target = "title", source = "proposal", qualifiedByName = "generateTitle")
    @Mapping(target = "photoId", source = "originalMediaFile.id")
    @Mapping(target = "submittedByUsername", source = "submittedBy.username")
    ProposalAdminListDto toAdminListDto(MarkOccurrenceProposal proposal);

    List<DecisionHistoryItem> toDecisionHistoryList(List<ProposalDecisionAttempt> decisions);

    @Mapping(target = "decidedBy", source = "decidedBy.id")
    DecisionHistoryItem toDecisionHistoryItem(ProposalDecisionAttempt attempt);

    @Mapping(target = "detectedMarkId", source = "detectedMark.id")
    @Mapping(target = "detectedMonumentId", source = "detectedMonument.id")
    @Mapping(target = "decidedBy", source = "decidedBy.id")
    ActiveDecisionViewDto toActiveDecisionViewDto(ProposalDecisionAttempt attempt);

    @Named("generateTitle")
    default String generateTitle(MarkOccurrenceProposal proposal) {
        if (proposal.getMonumentName() != null) {
            return proposal.getMonumentName();
        } else if (proposal.getExistingMonument() != null) {
            return proposal.getExistingMonument().getName();
        } else {
            return "Proposal #" + proposal.getId();
        }
    }
}
