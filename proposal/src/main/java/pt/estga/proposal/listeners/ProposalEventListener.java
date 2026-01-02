package pt.estga.proposal.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MonumentService;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.enums.ProposalStatus;
import pt.estga.proposal.events.ProposalAcceptedEvent;
import pt.estga.proposal.events.ProposalSubmittedEvent;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposal.services.AutomaticDecisionService;
import pt.estga.proposal.services.MonumentCreationService;
import pt.estga.proposal.services.ProposalScoringService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProposalEventListener {

    private final AutomaticDecisionService automaticDecisionService;
    private final MarkOccurrenceProposalRepository proposalRepo;
    private final MonumentCreationService monumentCreationService;
    private final ProposalScoringService proposalScoringService;
    private final MonumentService monumentService;

    @Async
    @EventListener
    public void handleProposalSubmitted(ProposalSubmittedEvent event) {

        MarkOccurrenceProposal proposal = event.getProposal();

        proposal.setPriority(proposalScoringService.calculatePriority(proposal));
        proposal.setCredibilityScore(proposalScoringService.calculateCredibilityScore(proposal));
        proposal.setStatus(ProposalStatus.SUBMITTED);

        proposalRepo.save(proposal);

        // Ensure monument exists (creates if needed and links to proposal)
        monumentCreationService.ensureMonumentExists(proposal);

        automaticDecisionService.run(proposal);
    }

    @Async
    @EventListener
    public void handleProposalAccepted(ProposalAcceptedEvent event) {
        MarkOccurrenceProposal proposal = event.getProposal();
        log.info("Handling accepted proposal ID: {}", proposal.getId());

        if (proposal.getExistingMonument() != null && !proposal.getExistingMonument().getActive()) {
            Monument monument = proposal.getExistingMonument();
            monument.setActive(true);
            monumentService.update(monument);
            log.info("Activated monument ID: {}", monument.getId());
        }
    }
}
