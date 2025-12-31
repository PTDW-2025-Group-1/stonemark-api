package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.ProposalDecisionAttempt;
import pt.estga.proposal.enums.DecisionOutcome;
import pt.estga.proposal.enums.DecisionType;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposal.repositories.ProposalDecisionAttemptRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomaticProposalDecisionService {

    private final ProposalDecisionAttemptRepository attemptRepo;
    private final MarkOccurrenceProposalRepository proposalRepo;

    public void run(MarkOccurrenceProposal proposal) {

        DecisionOutcome outcome;
        boolean confident = false;

        if (proposal.getPriority() > 150) {
            outcome = DecisionOutcome.ACCEPT;
            confident = true;
        } else if (proposal.getPriority() < 10) {
            outcome = DecisionOutcome.REJECT;
        } else {
            outcome = DecisionOutcome.INCONCLUSIVE;
        }

        ProposalDecisionAttempt attempt = ProposalDecisionAttempt.builder()
                .proposal(proposal)
                .type(DecisionType.AUTOMATIC)
                .outcome(outcome)
                .confident(confident)
                .notes("Priority-based automatic evaluation")
                .decidedAt(Instant.now())
                .build();

        attemptRepo.save(attempt);

        applyAsActiveDecision(proposal, attempt);
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
        } else {
            proposal.setStatus(ProposalStatus.UNDER_REVIEW);
        }

        proposalRepo.save(proposal);
    }
}
