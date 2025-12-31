package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
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
public class ManualDecisionService {

    private final ProposalDecisionAttemptRepository attemptRepo;
    private final MarkOccurrenceProposalRepository proposalRepo;

    public ProposalDecisionAttempt decide(
            MarkOccurrenceProposal proposal,
            DecisionOutcome outcome,
            String notes,
            Long moderatorId
    ) {

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

        proposal.setActiveDecision(attempt);
        proposal.setStatus(
            outcome == DecisionOutcome.ACCEPT
                ? ProposalStatus.MANUALLY_ACCEPTED
                : ProposalStatus.MANUALLY_REJECTED
        );

        proposalRepo.save(proposal);
        return attempt;
    }
}
