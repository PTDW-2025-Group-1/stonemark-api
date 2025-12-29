package pt.estga.chatbot.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.services.MarkOccurrenceProposalChatbotFlowService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class InitialPhotoHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        if (input.getType() != BotInput.InputType.PHOTO || input.getFileData() == null) {
            return HandlerOutcome.FAILURE;
        }

        try {
            // If there is no proposal in context, create a new one
            MarkOccurrenceProposal proposal = context.getProposalContext().getProposal();
            if (proposal == null) {
                proposal = proposalFlowService.startProposal(context.getDomainUserId());
                context.getProposalContext().setProposal(proposal);
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
