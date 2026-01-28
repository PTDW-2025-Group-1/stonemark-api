package pt.estga.decision.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.decision.dtos.ActiveDecisionViewDto;
import pt.estga.decision.dtos.DecisionHistoryItem;
import pt.estga.decision.dtos.ProposalAdminDetailDto;
import pt.estga.decision.entities.ProposalDecisionAttempt;
import pt.estga.decision.mappers.ProposalAdminMapper;
import pt.estga.decision.repositories.ProposalDecisionAttemptRepository;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposal.services.MonumentCreationService;
import pt.estga.shared.exceptions.ResourceNotFoundException;
import pt.estga.territory.dtos.GeocodingResultDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProposalAdminService {

    private final MarkOccurrenceProposalRepository proposalRepo;
    private final ProposalDecisionAttemptRepository attemptRepo;
    private final MonumentCreationService monumentCreationService;
    private final ProposalAdminMapper proposalAdminMapper;

    @Transactional(readOnly = true)
    public ProposalAdminDetailDto getProposalDetails(Long proposalId) {
        // Fetch proposal with eager relations (defined in repository)
        MarkOccurrenceProposal proposal = proposalRepo.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + proposalId));

        // Fetch all decision attempts with eager relations
        List<ProposalDecisionAttempt> attempts = attemptRepo.findByProposalIdOrderByDecidedAtDesc(proposalId);

        // Map history
        List<DecisionHistoryItem> history = proposalAdminMapper.toDecisionHistoryList(attempts);

        // Map active decision (latest one)
        ActiveDecisionViewDto activeDecision = attempts.stream()
                .findFirst()
                .map(proposalAdminMapper::toActiveDecisionViewDto)
                .orElse(null);

        return new ProposalAdminDetailDto(
                proposal.getId(),
                proposal.getStatus(),
                proposal.getPriority(),
                proposal.getCredibilityScore(),
                proposal.getSubmissionSource(),
                proposal.getUserNotes(),
                proposal.getMonumentName(),
                proposal.getLatitude(),
                proposal.getLongitude(),
                proposal.getOriginalMediaFile() != null ? proposal.getOriginalMediaFile().getId() : null,
                proposal.getSubmittedBy() != null ? proposal.getSubmittedBy().getId() : null,
                proposal.getSubmittedBy() != null ? proposal.getSubmittedBy().getUsername() : null,
                proposal.getSubmittedAt(),
                proposal.getExistingMonument() != null ? proposal.getExistingMonument().getId() : null,
                proposal.getExistingMonument() != null ? proposal.getExistingMonument().getName() : null,
                proposal.getExistingMark() != null ? proposal.getExistingMark().getId() : null,
                activeDecision,
                history
        );
    }
}
