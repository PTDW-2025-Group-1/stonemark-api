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
import pt.estga.proposal.entities.Proposal;
import pt.estga.proposal.services.MarkOccurrenceProposalChatbotFlowService;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class InitialPhotoHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;
    private final UserService userService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        if (input.getType() != BotInput.InputType.PHOTO || input.getFileData() == null) {
            return HandlerOutcome.FAILURE;
        }

        try {
            Proposal proposal = context.getProposalContext().getProposal();
            MarkOccurrenceProposal markProposal;

            if (proposal == null) {
                User user = userService.findById(context.getDomainUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                markProposal = proposalFlowService.startProposal(user);
                context.getProposalContext().setProposal(markProposal);
            } else if (proposal instanceof MarkOccurrenceProposal) {
                markProposal = (MarkOccurrenceProposal) proposal;
            } else {
                return HandlerOutcome.FAILURE;
            }

            proposalFlowService.addPhoto(
                    markProposal,
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
