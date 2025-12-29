package pt.estga.proposal.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.enums.SubmissionSource;
import pt.estga.proposal.events.ProposalSubmittedEvent;
import pt.estga.proposal.services.AutomaticProposalDecisionMaker;
import pt.estga.proposal.services.MarkOccurrenceProposalService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProposalEventListener {

    private final MarkOccurrenceProposalService proposalService;
    private final AutomaticProposalDecisionMaker decisionMaker;

    @Async
    @EventListener
    public void handleProposalSubmitted(ProposalSubmittedEvent event) {
        MarkOccurrenceProposal proposal = event.getProposal();
        log.info("Handling proposal submission event for proposal ID: {}", proposal.getId());

        Integer priority = calculatePriority(proposal);
        proposal.setPriority(priority);

        MarkOccurrenceProposal createdProposal = proposalService.create(proposal);
        log.info("Updated priority for proposal ID: {} to {}", createdProposal.getId(), priority);

        decisionMaker.makeDecision(createdProposal);
    }

    private Integer calculatePriority(MarkOccurrenceProposal proposal) {
        int priority = 0;

        // Boost for Staff Submissions (+50) - Significant boost
        if (proposal.getSubmissionSource() == SubmissionSource.STAFF_APP) {
            priority += 50;
        }

        // User Reputation Boost (based on approved proposals)
        Long userId = proposal.getSubmittedById();
        if (userId != null) {
            long approvedCount = proposalService.countApprovedProposalsByUserId(userId);
            
            // Cap the reputation boost at +40 (e.g., 2 points per approved proposal up to 40)
            int reputationBoost = (int) Math.min(approvedCount * 2, 40);
            priority += reputationBoost;
        }

        // Boost for New Monument Proposals (+5) - Small boost for complexity
        if (proposal.getProposedMonument() != null) {
            priority += 5;
        }

        return priority;
    }
}
