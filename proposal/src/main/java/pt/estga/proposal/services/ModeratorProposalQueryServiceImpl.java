package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.proposal.dtos.ActiveDecisionViewDto;
import pt.estga.proposal.dtos.DecisionHistoryItem;
import pt.estga.proposal.dtos.ProposalModeratorViewDto;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.ProposalDecisionAttempt;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposal.repositories.ProposalDecisionAttemptRepository;
import pt.estga.shared.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ModeratorProposalQueryServiceImpl implements ModeratorProposalQueryService {

    private final MarkOccurrenceProposalRepository proposalRepository;
    private final ProposalDecisionAttemptRepository decisionRepository;

    @Override
    public List<ProposalModeratorViewDto> getAllProposals() {
        List<MarkOccurrenceProposal> proposals = proposalRepository.findAllDetailed();

        return proposals.stream()
                .map(proposal -> {
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
                })
                .collect(Collectors.toList());
    }

    @Override
    public ProposalModeratorViewDto getProposal(Long id) {
        MarkOccurrenceProposal proposal = proposalRepository.findByIdDetailed(id)
                .orElseThrow(() -> {
                    log.error("Proposal with ID {} not found", id);
                    return new ResourceNotFoundException("Proposal not found with id: " + id);
                });

        return toModeratorViewDto(proposal);
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

    @Override
    public Page<ProposalModeratorViewDto> getAllProposals(List<ProposalStatus> statuses, Pageable pageable) {
        Page<MarkOccurrenceProposal> page;
        if (statuses != null && !statuses.isEmpty()) {
            page = proposalRepository.findByStatusIn(statuses, pageable);
        } else {
            page = proposalRepository.findAll(pageable);
        }
        return page.map(this::toModeratorViewDto);
    }

    private ProposalModeratorViewDto toModeratorViewDto(MarkOccurrenceProposal proposal) {
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
}
