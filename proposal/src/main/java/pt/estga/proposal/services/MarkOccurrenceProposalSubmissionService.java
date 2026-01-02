package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.events.ProposalSubmittedEvent;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarkOccurrenceProposalSubmissionService {

    private final MarkOccurrenceProposalService proposalService;
    private final ProposalScoringService scoringService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public MarkOccurrenceProposal submit(Long proposalId) {
        log.info("Submitting proposal with ID: {}", proposalId);
        MarkOccurrenceProposal proposal = proposalService.findById(proposalId)
                .orElseThrow(() -> {
                    log.error("Proposal with ID {} not found during submission", proposalId);
                    return new RuntimeException("Proposal not found");
                });

        // Calculate scores before submission
        Integer priority = scoringService.calculatePriority(proposal);
        Integer credibility = scoringService.calculateCredibilityScore(proposal);
        
        proposal.setPriority(priority);
        proposal.setCredibilityScore(credibility);
        log.debug("Calculated scores for proposal {}: Priority={}, Credibility={}", proposalId, priority, credibility);

        proposal.setSubmitted(true);
        proposal.setSubmittedAt(Instant.now());
        proposal.setStatus(ProposalStatus.SUBMITTED);

        MarkOccurrenceProposal updatedProposal = proposalService.update(proposal);
        log.info("Proposal with ID: {} submitted successfully", proposalId);
        
        eventPublisher.publishEvent(new ProposalSubmittedEvent(this, updatedProposal));
        log.debug("Published ProposalSubmittedEvent for proposal ID: {}", proposalId);

        return updatedProposal;
    }
}
