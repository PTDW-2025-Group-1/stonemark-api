package pt.estga.decision.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MonumentService;
import pt.estga.decision.services.MarkOccurrenceProposalDecisionService;
import pt.estga.proposal.events.ProposalAcceptedEvent;
import pt.estga.proposal.events.ProposalSubmittedEvent;
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
    public void handleProposalSubmitted(ProposalSubmittedEvent event) {
        Long proposalId = event.getProposalId();
        log.debug("Async processing of submitted proposal ID: {}", proposalId);

        proposalRepo.findById(proposalId).ifPresentOrElse(
                markOccurrenceProposalDecisionService::makeAutomaticDecision,
                () -> log.error("Proposal with ID {} not found during async processing", proposalId)
        );
    }

    @Async
    @EventListener
    @Transactional
    public void handleProposalAccepted(ProposalAcceptedEvent event) {
        Long proposalId = event.getProposal().getId();
        log.debug("Async processing of accepted proposal ID: {}", proposalId);

        proposalRepo.findById(proposalId).ifPresent(proposal -> {
            if (proposal.getExistingMonument() != null && !proposal.getExistingMonument().getActive()) {
                Monument monument = proposal.getExistingMonument();
                monument.setActive(true);
                monumentService.update(monument);
                log.info("Activated monument ID: {}", monument.getId());
            }
        });
    }
}
