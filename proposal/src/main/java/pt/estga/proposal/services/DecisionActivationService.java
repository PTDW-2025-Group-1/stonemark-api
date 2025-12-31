package pt.estga.proposal.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.ProposalDecisionAttempt;
import pt.estga.proposal.enums.DecisionOutcome;
import pt.estga.proposal.enums.DecisionType;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposal.repositories.ProposalDecisionAttemptRepository;

@Service
@RequiredArgsConstructor
public class DecisionActivationService {

    private final MarkOccurrenceProposalRepository proposalRepo;
    private final ProposalDecisionAttemptRepository attemptRepo;

    @Transactional
    public void activateDecision(Long proposalId, Long attemptId) {
        MarkOccurrenceProposal proposal = proposalRepo.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        ProposalDecisionAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Decision attempt not found"));

        if (!attempt.getProposal().getId().equals(proposalId)) {
            throw new IllegalArgumentException("Decision attempt does not belong to this proposal");
        }

        proposal.setActiveDecision(attempt);

        ProposalStatus newStatus;
        if (attempt.getType() == DecisionType.MANUAL) {
            newStatus = attempt.getOutcome() == DecisionOutcome.ACCEPT
                    ? ProposalStatus.MANUALLY_ACCEPTED
                    : ProposalStatus.MANUALLY_REJECTED;
        } else {
            // Automatic
            if (attempt.getOutcome() == DecisionOutcome.ACCEPT) {
                newStatus = ProposalStatus.AUTO_ACCEPTED;
            } else if (attempt.getOutcome() == DecisionOutcome.REJECT) {
                newStatus = ProposalStatus.AUTO_REJECTED;
            } else {
                newStatus = ProposalStatus.UNDER_REVIEW;
            }
        }
        proposal.setStatus(newStatus);

        proposalRepo.save(proposal);
    }
}
