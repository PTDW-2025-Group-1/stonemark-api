package pt.estga.chatbots.core.features.proposal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationProcessorService {

    private final MarkOccurrenceProposalFlowService proposalFlowService;

    public MarkOccurrenceProposal processLocation(ConversationContext context, double latitude, double longitude) {
        log.info("Processing location for proposal ID: {}", context.getProposal().getId());
        MarkOccurrenceProposal proposal = context.getProposal();
        MarkOccurrenceProposal updatedProposal = proposalFlowService.addLocationToProposal(proposal.getId(), latitude, longitude);
        context.setProposal(updatedProposal);
        return updatedProposal;
    }
}
