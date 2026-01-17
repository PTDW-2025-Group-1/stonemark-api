package pt.estga.proposal.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.proposal.dtos.DecisionHistoryItem;
import pt.estga.proposal.dtos.ProposalModeratorListDto;
import pt.estga.proposal.dtos.ProposalModeratorViewDto;
import pt.estga.proposal.enums.ProposalStatus;

import java.util.List;

public interface ModeratorProposalQueryService {

    ProposalModeratorViewDto getProposal(Long id);

    List<DecisionHistoryItem> getDecisionHistory(Long proposalId);

    Page<ProposalModeratorListDto> getAllProposals(List<ProposalStatus> statuses, Pageable pageable);

}
