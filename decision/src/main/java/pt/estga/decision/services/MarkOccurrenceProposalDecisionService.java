package pt.estga.decision.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.decision.entities.ProposalDecisionAttempt;
import pt.estga.decision.enums.DecisionOutcome;
import pt.estga.decision.enums.DecisionType;
import pt.estga.decision.repositories.ProposalDecisionAttemptRepository;
import pt.estga.decision.rules.DecisionRule;
import pt.estga.decision.rules.DecisionRuleResult;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.events.ProposalAcceptedEvent;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class MarkOccurrenceProposalDecisionService extends ProposalDecisionService<MarkOccurrenceProposal> {

    private final List<DecisionRule<MarkOccurrenceProposal>> rules;

    public MarkOccurrenceProposalDecisionService(
            ProposalDecisionAttemptRepository attemptRepo,
            MarkOccurrenceProposalRepository proposalRepo,
            ApplicationEventPublisher eventPublisher,
            List<DecisionRule<MarkOccurrenceProposal>> rules
    ) {
        super(attemptRepo, proposalRepo, eventPublisher, MarkOccurrenceProposal.class);
        this.rules = rules;
    }

    /**
     * Automatically evaluates a proposal based on a set of injected rules.
     * <p>
     * The rules are evaluated in order (defined by {@link DecisionRule#getOrder()}).
     * The first rule to return a conclusive result determines the outcome.
     * If no rule matches, the outcome defaults to INCONCLUSIVE.
     *
     * @param proposal The proposal to evaluate.
     * @return The created decision attempt.
     */
    @Override
    @Transactional
    public ProposalDecisionAttempt makeAutomaticDecision(MarkOccurrenceProposal proposal) {
        log.debug("Running automatic decision logic for proposal ID: {}, Priority: {}", proposal.getId(), proposal.getPriority());

        DecisionOutcome outcome = DecisionOutcome.INCONCLUSIVE;
        var confident = false;
        var notes = "No rule matched; defaulting to inconclusive.";

        // Sort rules by order
        var sortedRules = rules.stream()
                .sorted(Comparator.comparingInt(DecisionRule::getOrder))
                .toList();

        for (var rule : sortedRules) {
            var result = rule.evaluate(proposal);
            if (result != null) {
                outcome = result.outcome();
                confident = result.confident();
                notes = result.reason();
                log.info("Rule '{}' matched for proposal ID {}: {}", rule.getClass().getSimpleName(), proposal.getId(), result);
                break;
            }
        }

        log.info("Automatic decision outcome for proposal ID {}: {} (Confident: {})", proposal.getId(), outcome, confident);

        var attempt = ProposalDecisionAttempt.builder()
                .proposal(proposal)
                .type(DecisionType.AUTOMATIC)
                .outcome(outcome)
                .confident(confident)
                .notes(notes)
                .decidedAt(Instant.now())
                .build();

        return saveAndApplyDecision(proposal, attempt);
    }

    @Override
    protected void validateManualDecision(MarkOccurrenceProposal proposal, DecisionOutcome outcome) {
        // If accepting, and it's a new monument, ensure monument is created
        if (outcome == DecisionOutcome.ACCEPT && isNewMonumentProposal(proposal)) {
            throw new IllegalStateException("Cannot approve proposal for new monument without creating the monument first.");
        }
    }

    @Override
    protected void publishAcceptedEvent(MarkOccurrenceProposal proposal) {
        eventPublisher.publishEvent(new ProposalAcceptedEvent(this, proposal));
    }

    private boolean isNewMonumentProposal(MarkOccurrenceProposal proposal) {
        return proposal.getExistingMonument() == null && proposal.getProposedMonument() != null;
    }
}
