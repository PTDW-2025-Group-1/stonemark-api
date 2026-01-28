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
import pt.estga.user.entities.User;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProposalDecisionService {

    private final ProposalDecisionAttemptRepository attemptRepo;
    private final MarkOccurrenceProposalRepository proposalRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final ProposalDecisionProperties properties;

    /**
     * Triggers the automatic decision logic for a proposal by ID.
     */
    @Transactional
    public ProposalDecisionAttempt makeAutomaticDecision(Long proposalId) {
        log.info("Processing automatic decision for proposal ID: {}", proposalId);
        MarkOccurrenceProposal proposal = getProposalOrThrow(proposalId);
        return makeAutomaticDecision(proposal);
    }

    /**
     * Triggers the automatic decision logic for a proposal entity.
     */
    @Transactional
    public ProposalDecisionAttempt makeAutomaticDecision(MarkOccurrenceProposal proposal) {
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

        return saveAndApplyDecision(proposal, attempt);
    }

    /**
     * Creates a manual decision for a proposal.
     */
    @Transactional
    public ProposalDecisionAttempt makeManualDecision(Long proposalId, DecisionOutcome outcome, String notes, User moderator) {
        log.info("Creating manual decision for proposal ID: {}, Outcome: {}, Moderator ID: {}", proposalId, outcome, moderator.getId());
        MarkOccurrenceProposal proposal = getProposalOrThrow(proposalId);

        // If accepting, and it's a new monument, ensure monument is created
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
                .decidedBy(moderator)
                .build();

        return saveAndApplyDecision(proposal, attempt);
    }

    /**
     * Activates a specific decision attempt for a proposal.
     */
    @Transactional
    public void activateDecision(Long attemptId) {
        ProposalDecisionAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision attempt not found with id: " + attemptId));
        
        MarkOccurrenceProposal proposal = attempt.getProposal();
        log.info("Activating decision attempt ID: {} for proposal ID: {}", attemptId, proposal.getId());

        saveAndApplyDecision(proposal, attempt);
    }

    /**
     * Deactivates the current decision for a proposal, reverting it to UNDER_REVIEW status.
     */
    @Transactional
    public void deactivateDecision(Long proposalId) {
        MarkOccurrenceProposal proposal = getProposalOrThrow(proposalId);
        log.info("Deactivating decision for proposal ID: {}", proposalId);

        if (proposal.getActiveDecision() == null) {
            log.warn("Proposal ID {} has no active decision to deactivate", proposalId);
            return;
        }

        proposal.setActiveDecision(null);
        proposal.setStatus(ProposalStatus.UNDER_REVIEW);
        proposalRepo.save(proposal);

        log.info("Successfully deactivated decision for proposal ID: {}. Status reverted to UNDER_REVIEW", proposalId);
    }

    // ==== Helper Methods ====

    private ProposalDecisionAttempt saveAndApplyDecision(MarkOccurrenceProposal proposal, ProposalDecisionAttempt attempt) {
        attemptRepo.save(attempt);
        log.debug("Saved decision attempt with ID: {}", attempt.getId());

        proposal.applyDecision(attempt);
        proposalRepo.save(proposal);
        log.info("Updated proposal ID: {} status to: {}", proposal.getId(), proposal.getStatus());

        if (attempt.getOutcome() == DecisionOutcome.ACCEPT) {
            eventPublisher.publishEvent(new ProposalAcceptedEvent(this, proposal));
        }

        return attempt;
    }

    private MarkOccurrenceProposal getProposalOrThrow(Long id) {
        return proposalRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Proposal with ID {} not found", id);
                    return new ResourceNotFoundException("Proposal not found with id: " + id);
                });
    }
}
