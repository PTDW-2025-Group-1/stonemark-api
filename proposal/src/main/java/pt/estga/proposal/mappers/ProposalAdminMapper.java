package pt.estga.proposal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pt.estga.proposal.dtos.DecisionHistoryItem;
import pt.estga.proposal.dtos.ProposalAdminListDto;
import pt.estga.proposal.dtos.ProposalModeratorViewDto;
import pt.estga.proposal.dtos.ProposalWithRelationsDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.ProposalDecisionAttempt;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProposalAdminMapper {

    @Mapping(target = "submittedById", source = "submittedBy.id")
    @Mapping(target = "submittedByUsername", source = "submittedBy.username")
    @Mapping(target = "activeDecision", source = "activeDecision")
    @Mapping(target = "photoId", source = "originalMediaFile.id")
    @Mapping(target = "existingMonumentId", source = "existingMonument.id")
    @Mapping(target = "existingMonumentName", source = "existingMonument.name")
    @Mapping(target = "existingMarkId", source = "existingMark.id")
    @Mapping(target = "existingMarkName", source = "existingMark.name")
    ProposalModeratorViewDto toModeratorViewDto(MarkOccurrenceProposal proposal);

    @Mapping(target = "title", source = "proposal", qualifiedByName = "generateTitle")
    @Mapping(target = "photoId", source = "originalMediaFile.id")
    @Mapping(target = "submittedByUsername", source = "submittedBy.username")
    ProposalAdminListDto toAdminListDto(MarkOccurrenceProposal proposal);

    List<DecisionHistoryItem> toDecisionHistoryList(List<ProposalDecisionAttempt> decisions);

    ProposalWithRelationsDto toWithRelationsDto(MarkOccurrenceProposal proposal);

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
