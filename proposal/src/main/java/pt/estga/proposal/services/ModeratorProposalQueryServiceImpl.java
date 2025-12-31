package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.proposal.dtos.ActiveDecisionViewDto;
import pt.estga.proposal.dtos.DecisionHistoryItem;
import pt.estga.proposal.dtos.ProposalModeratorViewDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.ProposalDecisionAttempt;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposal.repositories.ProposalDecisionAttemptRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModeratorProposalQueryServiceImpl implements ModeratorProposalQueryService {

    private final MarkOccurrenceProposalRepository proposalRepository;
    private final ProposalDecisionAttemptRepository decisionRepository;

    @Override
    public ProposalModeratorViewDto getProposal(Long id) {
        MarkOccurrenceProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        ActiveDecisionViewDto activeDecisionDto = getActiveDecisionViewDto(proposal);

        return new ProposalModeratorViewDto(
                proposal.getId(),
                proposal.getStatus(),
                proposal.getPriority(),
                proposal.getSubmissionSource(),
                proposal.getSubmittedById(),
                proposal.getSubmittedAt(),
                proposal.getMonumentName(),
                proposal.getLatitude(),
                proposal.getLongitude(),
                proposal.getUserNotes(),
                activeDecisionDto
        );
    }

    private static @Nullable ActiveDecisionViewDto getActiveDecisionViewDto(MarkOccurrenceProposal proposal) {
        ActiveDecisionViewDto activeDecisionDto = null;
        if (proposal.getActiveDecision() != null) {
            ProposalDecisionAttempt decision = proposal.getActiveDecision();
            activeDecisionDto = new ActiveDecisionViewDto(
                    decision.getId(),
                    decision.getType(),
                    decision.getOutcome(),
                    decision.getConfident(),
                    decision.getDetectedMark(),
                    decision.getDetectedMonument(),
                    decision.getNotes(),
                    decision.getDecidedAt(),
                    decision.getDecidedBy()
            );
        }
        return activeDecisionDto;
    }

    @Override
    public List<DecisionHistoryItem> getDecisionHistory(Long proposalId) {
        return decisionRepository.findByProposalIdOrderByDecidedAtDesc(proposalId)
                .stream()
                .map(decision -> new DecisionHistoryItem(
                        decision.getId(),
                        decision.getType(),
                        decision.getOutcome(),
                        decision.getConfident(),
                        decision.getDecidedAt(),
                        decision.getDecidedBy(),
                        decision.getNotes()
                ))
                .collect(Collectors.toList());
    }
}
