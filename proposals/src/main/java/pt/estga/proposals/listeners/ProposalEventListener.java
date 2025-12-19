package pt.estga.proposals.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.ProposalPriority;
import pt.estga.proposals.events.ProposalSubmittedEvent;
import pt.estga.proposals.repositories.MarkOccurrenceProposalRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProposalEventListener {

    private final MarkOccurrenceProposalRepository proposalRepository;

    @Async
    @EventListener
    public void handleProposalSubmitted(ProposalSubmittedEvent event) {
        MarkOccurrenceProposal proposal = event.getProposal();
        log.info("Handling proposal submission event for proposal ID: {}", proposal.getId());

        // Logic to determine priority
        // For now, we can set a default or based on some simple logic
        // Example: If it has a proposed mark, give it HIGH priority
        if (proposal.getProposedMark() != null) {
            proposal.setPriority(ProposalPriority.HIGH);
        } else {
            proposal.setPriority(ProposalPriority.MEDIUM);
        }

        proposalRepository.save(proposal);
        log.info("Updated priority for proposal ID: {} to {}", proposal.getId(), proposal.getPriority());
    }
}
