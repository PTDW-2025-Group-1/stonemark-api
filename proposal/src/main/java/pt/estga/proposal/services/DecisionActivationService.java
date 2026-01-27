package pt.estga.proposal.services;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.ProposalDecisionAttempt;
import pt.estga.proposal.enums.DecisionOutcome;
import pt.estga.proposal.enums.DecisionType;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposal.repositories.ProposalDecisionAttemptRepository;
import pt.estga.shared.exceptions.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DecisionActivationService {

    private final MarkOccurrenceProposalRepository proposalRepo;
    private final ProposalDecisionAttemptRepository attemptRepo;

    @Transactional
    public void activateDecision(Long proposalId, Long attemptId) {
        log.info("Activating decision attempt ID: {} for proposal ID: {}", attemptId, proposalId);
        
        MarkOccurrenceProposal proposal = proposalRepo.findDetailedById(proposalId)
                .orElseThrow(() -> {
                    log.error("Proposal with ID {} not found during decision activation", proposalId);
                    return new ResourceNotFoundException("Proposal not found with id: " + proposalId);
                });

        ProposalDecisionAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> {
                    log.error("Decision attempt with ID {} not found", attemptId);
                    return new ResourceNotFoundException("Decision attempt not found with id: " + attemptId);
                });

        if (!attempt.getProposal().getId().equals(proposalId)) {
            log.error("Decision attempt ID {} does not belong to proposal ID {}", attemptId, proposalId);
            throw new IllegalArgumentException("Decision attempt does not belong to this proposal");
        }

        proposal.setActiveDecision(attempt);

        ProposalStatus newStatus = getProposalStatus(attempt);
        proposal.setStatus(newStatus);

        proposalRepo.save(proposal);
        log.info("Successfully activated decision attempt ID: {}. Proposal ID: {} status updated to: {}", attemptId, proposalId, newStatus);
    }

    private static @NonNull ProposalStatus getProposalStatus(ProposalDecisionAttempt attempt) {
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
        return newStatus;
    }
}
