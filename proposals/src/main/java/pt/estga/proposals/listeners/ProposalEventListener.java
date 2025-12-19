package pt.estga.proposals.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.SubmissionSource;
import pt.estga.proposals.events.ProposalSubmittedEvent;
import pt.estga.proposals.services.AutomaticProposalDecisionMaker;
import pt.estga.proposals.services.MarkOccurrenceProposalService;
import pt.estga.user.entities.User;

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

        MarkOccurrenceProposal savedProposal = proposalService.save(proposal);
        log.info("Updated priority for proposal ID: {} to {}", savedProposal.getId(), priority);

        decisionMaker.makeDecision(savedProposal);
    }

    private Integer calculatePriority(MarkOccurrenceProposal proposal) {
        int priority = 0;

        // 1. Boost for Staff Submissions (+50) - Significant boost
        if (proposal.getSubmissionSource() == SubmissionSource.STAFF_APP) {
            priority += 50;
        }

        // 2. User Reputation Boost (based on approved proposals)
        User user = proposal.getCreatedBy();
        if (user != null) {
            long approvedCount = proposalService.countApprovedProposalsByUser(user);
            
            // Cap the reputation boost at +40 (e.g., 2 points per approved proposal up to 40)
            int reputationBoost = (int) Math.min(approvedCount * 2, 40);
            priority += reputationBoost;
        }

        // 3. Boost for New Mark Proposals (+5) - Small boost for complexity
        if (proposal.getProposedMark() != null) {
            priority += 5;
        }

        // 4. Boost for New Monument Proposals (+5) - Small boost for complexity
        if (proposal.getProposedMonument() != null) {
            priority += 5;
        }

        return priority;
    }
}
