package pt.estga.chatbots.core.features.proposal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PhotoProcessorService {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final MarkProcessorService markProcessorService;

    public BotResponse processPhoto(ConversationContext context, BotInput input) {
        try {
            Long domainUserId = context.getDomainUserId();
            log.info("Initiating proposal for user ID: {} (domain ID: {}) with file: {}", input.getUserId(), domainUserId, input.getFileName());
            MarkOccurrenceProposal proposal = proposalFlowService.initiate(domainUserId, input.getFileData(), input.getFileName());
            context.setProposal(proposal);
            log.info("Proposal with ID {} created.", proposal.getId());

            return markProcessorService.processMarkSuggestions(context, proposal);

        } catch (IOException e) {
            log.error("Error processing photo for user: {}", input.getUserId(), e);
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing photo.").build())
                    .build();
        }
    }
}
