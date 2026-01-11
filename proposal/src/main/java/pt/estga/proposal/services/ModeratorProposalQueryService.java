package pt.estga.proposal.services;

import pt.estga.proposal.dtos.DecisionHistoryItem;
import pt.estga.proposal.dtos.ProposalModeratorViewDto;

import java.util.List;

public interface ModeratorProposalQueryService {

    List<ProposalModeratorViewDto> getAllProposals();

    ProposalModeratorViewDto getProposal(Long id);

    List<DecisionHistoryItem> getDecisionHistory(Long proposalId);

}
