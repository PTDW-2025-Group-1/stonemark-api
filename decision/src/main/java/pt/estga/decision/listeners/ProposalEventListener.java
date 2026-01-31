package pt.estga.decision.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.services.MonumentService;
import pt.estga.decision.services.MarkOccurrenceProposalDecisionService;
import pt.estga.proposal.events.ProposalAcceptedEvent;
import pt.estga.proposal.events.ProposalScoredEvent;
import pt.estga.proposal.repositories.MarkOccurrenceProposalRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProposalEventListener {

    private final MarkOccurrenceProposalDecisionService markOccurrenceProposalDecisionService;
    private final MarkOccurrenceProposalRepository proposalRepo;
    private final MonumentService monumentService;

    @Async
    @EventListener
    @Transactional
    public void handleProposalScored(ProposalScoredEvent event) {
        var proposalId = event.getProposalId();
        log.info("Starting async automatic decision process for proposal ID: {}", proposalId);

        proposalRepo.findById(proposalId).ifPresentOrElse(
                proposal -> {
                    markOccurrenceProposalDecisionService.makeAutomaticDecision(proposal);
                    log.info("Completed async automatic decision process for proposal ID: {}", proposalId);
                },
                () -> log.error("Proposal with ID {} not found during async processing", proposalId)
        );
    }

    @Async
    @EventListener
    @Transactional
    public void handleProposalAccepted(ProposalAcceptedEvent event) {
        var proposalId = event.getProposal().getId();
        log.info("Starting async acceptance processing for proposal ID: {}", proposalId);

        proposalRepo.findById(proposalId).ifPresentOrElse(proposal -> {
            if (proposal.getExistingMonument() != null && !proposal.getExistingMonument().getActive()) {
                var monument = proposal.getExistingMonument();
                monument.setActive(true);
                monumentService.update(monument);
                log.info("Activated monument ID: {} as part of proposal acceptance", monument.getId());
            }
            log.info("Completed async acceptance processing for proposal ID: {}", proposalId);
        }, () -> log.error("Proposal with ID {} not found during async acceptance processing", proposalId));
    }
}
