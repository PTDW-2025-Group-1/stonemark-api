package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.ProposalDecisionAttempt;
import pt.estga.proposal.enums.DecisionOutcome;
import pt.estga.proposal.enums.DecisionType;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.events.ProposalAcceptedEvent;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposal.repositories.ProposalDecisionAttemptRepository;
import pt.estga.shared.exceptions.ResourceNotFoundException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManualDecisionService {

    private final ProposalDecisionAttemptRepository attemptRepo;
    private final MarkOccurrenceProposalRepository proposalRepo;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ProposalDecisionAttempt createManualDecision(Long proposalId, DecisionOutcome outcome, String notes, Long moderatorId) {
        log.info("Creating manual decision for proposal ID: {}, Outcome: {}, Moderator ID: {}", proposalId, outcome, moderatorId);
        
        MarkOccurrenceProposal proposal = proposalRepo.findById(proposalId)
                .orElseThrow(() -> {
                    log.error("Proposal with ID {} not found during manual decision creation", proposalId);
                    return new ResourceNotFoundException("Proposal not found with id: " + proposalId);
                });

        // If accepting and it's a new monument, ensure monument is created
        if (outcome == DecisionOutcome.ACCEPT && proposal.getExistingMonument() == null && proposal.getMonumentName() != null) {
             throw new IllegalStateException("Cannot approve proposal for new monument without creating the monument first.");
        }

        ProposalDecisionAttempt attempt = ProposalDecisionAttempt.builder()
                .proposal(proposal)
                .type(DecisionType.MANUAL)
                .outcome(outcome)
                .confident(true)
                .notes(notes)
                .decidedAt(Instant.now())
                .decidedBy(moderatorId)
                .build();

        attemptRepo.save(attempt);
        log.debug("Saved manual decision attempt with ID: {}", attempt.getId());

        proposal.setActiveDecision(attempt);
        proposal.setStatus(
            outcome == DecisionOutcome.ACCEPT
                ? ProposalStatus.MANUALLY_ACCEPTED
                : ProposalStatus.MANUALLY_REJECTED
        );

        proposalRepo.save(proposal);
        log.info("Updated proposal ID: {} status to: {}", proposalId, proposal.getStatus());

        if (outcome == DecisionOutcome.ACCEPT) {
            eventPublisher.publishEvent(new ProposalAcceptedEvent(this, proposal));
        }

        return attempt;
    }
}
