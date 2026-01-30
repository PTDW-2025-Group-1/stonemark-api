package pt.estga.decision.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.decision.entities.ProposalDecisionAttempt;
import pt.estga.decision.enums.DecisionOutcome;
import pt.estga.decision.enums.DecisionType;
import pt.estga.decision.repositories.ProposalDecisionAttemptRepository;
import pt.estga.proposal.config.ProposalDecisionProperties;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.events.ProposalAcceptedEvent;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;

import java.time.Instant;

@Service
@Slf4j
public class MarkOccurrenceProposalDecisionService extends ProposalDecisionService<MarkOccurrenceProposal> {

    private final ProposalDecisionProperties properties;

    public MarkOccurrenceProposalDecisionService(
            ProposalDecisionAttemptRepository attemptRepo,
            MarkOccurrenceProposalRepository proposalRepo,
            ApplicationEventPublisher eventPublisher,
            ProposalDecisionProperties properties
    ) {
        super(attemptRepo, proposalRepo, eventPublisher, MarkOccurrenceProposal.class);
        this.properties = properties;
    }

    @Override
    @Transactional
    public ProposalDecisionAttempt makeAutomaticDecision(MarkOccurrenceProposal proposal) {
        log.debug("Running automatic decision logic for proposal ID: {}, Priority: {}", proposal.getId(), proposal.getPriority());

        DecisionOutcome outcome;
        boolean confident = false;

        // If the proposal requires a new monument, it must be reviewed manually
        if (Boolean.TRUE.equals(properties.getRequireManualReviewForNewMonuments()) &&
                proposal.getExistingMonument() == null && proposal.getProposedMonument() != null) {
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

    @Override
    protected void validateManualDecision(MarkOccurrenceProposal proposal, DecisionOutcome outcome) {
        // If accepting, and it's a new monument, ensure monument is created
        if (outcome == DecisionOutcome.ACCEPT && proposal.getExistingMonument() == null && proposal.getProposedMonument() != null) {
            throw new IllegalStateException("Cannot approve proposal for new monument without creating the monument first.");
        }
    }

    @Override
    protected void publishAcceptedEvent(MarkOccurrenceProposal proposal) {
        eventPublisher.publishEvent(new ProposalAcceptedEvent(this, proposal));
    }
}
