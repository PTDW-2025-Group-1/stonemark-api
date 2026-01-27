package pt.estga.proposal.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.estga.proposal.dtos.DecisionHistoryItem;
import pt.estga.proposal.dtos.ProposalAdminListDto;
import pt.estga.proposal.dtos.ProposalModeratorViewDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.projections.MarkOccurrenceProposalStatsProjection;
import pt.estga.user.entities.User;

import java.util.List;
import java.util.Optional;

public interface MarkOccurrenceProposalService {

    Page<MarkOccurrenceProposal> getAll(Pageable pageable);

    Optional<MarkOccurrenceProposal> findById(Long id);

    Optional<MarkOccurrenceProposal> findWithDetailsById(Long id);

    Page<MarkOccurrenceProposal> findByUser(User user, Pageable pageable);

    MarkOccurrenceProposal create(MarkOccurrenceProposal proposal);

    MarkOccurrenceProposal update(MarkOccurrenceProposal proposal);

    void delete(MarkOccurrenceProposal proposal);

    MarkOccurrenceProposalStatsProjection getStatsByUser(User user);

    long countApprovedProposalsByUserId(Long userId);

    // Admin/Moderator methods
    Page<ProposalAdminListDto> getAdminProposals(List<ProposalStatus> statuses, Pageable pageable);

    ProposalModeratorViewDto getAdminProposalDetails(Long id);

    List<DecisionHistoryItem> getDecisionHistory(Long proposalId);
}
