package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.proposal.config.ProposalDecisionProperties;
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
public class AutomaticDecisionService {

    private final ProposalDecisionAttemptRepository attemptRepo;
    private final MarkOccurrenceProposalRepository proposalRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final ProposalDecisionProperties properties;

    @Transactional
    public ProposalDecisionAttempt rerunAutomaticDecision(Long proposalId) {
        log.info("Rerunning automatic decision for proposal ID: {}", proposalId);
        MarkOccurrenceProposal proposal = proposalRepo.findById(proposalId)
                .orElseThrow(() -> {
                    log.error("Proposal with ID {} not found during automatic decision rerun", proposalId);
                    return new ResourceNotFoundException("Proposal not found with id: " + proposalId);
                });

        return run(proposal);
    }

    @Transactional
    public ProposalDecisionAttempt run(MarkOccurrenceProposal proposal) {
        log.debug("Running automatic decision logic for proposal ID: {}, Priority: {}", proposal.getId(), proposal.getPriority());

        DecisionOutcome outcome;
        boolean confident = false;

        // If the proposal requires a new monument, it must be reviewed manually
        if (proposal.getExistingMonument() == null && proposal.getMonumentName() != null) {
            log.info("Proposal ID {} requires new monument creation. Marking as inconclusive for manual review.", proposal.getId());
            outcome = DecisionOutcome.INCONCLUSIVE;
            confident = true; // Confidently sending to manual review
        } else if (proposal.getPriority() != null && proposal.getPriority() > properties.getAutomaticAcceptanceThreshold()) {
            outcome = DecisionOutcome.ACCEPT;
            confident = true;
        } else if (proposal.getPriority() != null && proposal.getPriority() < properties.getAutomaticRejectionThreshold()) {
            outcome = DecisionOutcome.REJECT;
        } else {
            outcome = DecisionOutcome.INCONCLUSIVE;
        }

        log.info("Automatic decision outcome for proposal ID {}: {} (Confident: {})", proposal.getId(), outcome, confident);

        ProposalDecisionAttempt attempt = ProposalDecisionAttempt.builder()
                .proposal(proposal)
                .type(DecisionType.AUTOMATIC)
                .outcome(outcome)
                .confident(confident)
                .notes("Priority-based automatic evaluation")
                .decidedAt(Instant.now())
                .build();

        attemptRepo.save(attempt);
        log.debug("Saved automatic decision attempt with ID: {}", attempt.getId());

        applyAsActiveDecision(proposal, attempt);
        
        return attempt;
    }

    private void applyAsActiveDecision(
            MarkOccurrenceProposal proposal,
            ProposalDecisionAttempt attempt
    ) {
        proposal.setActiveDecision(attempt);

        if (attempt.getOutcome().isFinal()) {
            proposal.setStatus(
                attempt.getOutcome() == DecisionOutcome.ACCEPT
                    ? ProposalStatus.AUTO_ACCEPTED
                    : ProposalStatus.AUTO_REJECTED
            );

            if (attempt.getOutcome() == DecisionOutcome.ACCEPT) {
                eventPublisher.publishEvent(new ProposalAcceptedEvent(this, proposal));
            }
        } else {
            // If it's inconclusive because of a new monument, we might want a specific status
            if (proposal.getExistingMonument() == null && proposal.getMonumentName() != null) {
                proposal.setStatus(ProposalStatus.PENDING_MONUMENT_CREATION);
            } else {
                proposal.setStatus(ProposalStatus.UNDER_REVIEW);
            }
        }

        proposalRepo.save(proposal);
        log.info("Updated proposal ID: {} status to: {}", proposal.getId(), proposal.getStatus());
    }
}
