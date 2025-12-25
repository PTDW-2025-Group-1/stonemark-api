package pt.estga.chatbots.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.context.ConversationContext;
import pt.estga.chatbots.context.ConversationState;
import pt.estga.chatbots.context.ConversationStateHandler;
import pt.estga.chatbots.context.HandlerOutcome;
import pt.estga.chatbots.context.ProposalState;
import pt.estga.chatbots.models.BotInput;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class InitialPhotoHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        if (input.getType() != BotInput.InputType.PHOTO || input.getFileData() == null) {
            return HandlerOutcome.FAILURE;
        }

        try {
            // If there is no proposal in context, create a new one
            MarkOccurrenceProposal proposal = context.getProposal();
            if (proposal == null) {
                proposal = proposalFlowService.startProposal(context.getDomainUserId());
                context.setProposal(proposal);
            }

            // Add the photo to the proposal
            proposalFlowService.addPhoto(
                    proposal.getId(),
                    input.getFileData(),
                    input.getFileName()
            );

            return HandlerOutcome.SUCCESS;
        } catch (IOException e) {
            return HandlerOutcome.FAILURE;
        }
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.WAITING_FOR_PHOTO;
    }
}
