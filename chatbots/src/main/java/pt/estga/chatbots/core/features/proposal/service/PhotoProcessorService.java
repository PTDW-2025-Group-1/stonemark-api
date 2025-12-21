package pt.estga.chatbots.core.features.proposal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PhotoProcessorService {

    private final MarkOccurrenceProposalFlowService proposalFlowService;

    public MarkOccurrenceProposal processPhoto(ConversationContext context, BotInput input) throws IOException {
        MarkOccurrenceProposal proposal = context.getProposal();
        if (proposal == null) {
            Long domainUserId = context.getDomainUserId();
            log.info("Initiating proposal for user ID: {} (domain ID: {}) with file: {}",
                    input.getUserId(), domainUserId, input.getFileName());
            proposal = proposalFlowService.initiate(
                    domainUserId, input.getFileData(), input.getFileName(),
                    input.getLocation().getLatitude(), input.getLocation().getLongitude()
            );
            context.setProposal(proposal);
            log.info("Proposal with ID {} created.", proposal.getId());
        } else {
            log.info("Updating proposal ID {} with new photo: {}", proposal.getId(), input.getFileName());
            proposal = proposalFlowService.updatePhoto(proposal.getId(), input.getFileData(), input.getFileName());
            context.setProposal(proposal);
            log.info("Proposal with ID {} updated.", proposal.getId());
            context.setCurrentState(ConversationState.LOOP_OPTIONS);
        }
        return proposal;
    }
}
