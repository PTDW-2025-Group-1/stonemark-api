package pt.estga.chatbots.telegram.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import pt.estga.chatbots.telegram.context.ConversationContext;
import pt.estga.chatbots.telegram.message.TelegramBotMessageFactory;
import pt.estga.chatbots.telegram.state.factory.StateFactory;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AwaitingPhotoState implements ConversationState {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final TelegramBotMessageFactory messageFactory;
    private final StateFactory stateFactory;

    @Override
    public ProposalStatus getAssociatedStatus() {
        return ProposalStatus.AWAITING_PHOTO;
    }

    @Override
    public BotApiMethod<?> handlePhotoSubmission(ConversationContext context, byte[] photoData, String fileName) {
        try {
            MarkOccurrenceProposal proposal = proposalFlowService.initiate(context.getUserId(), photoData, fileName);
            context.setProposal(proposal);
            context.setProposalId(proposal.getId());
            context.setState(stateFactory.createState(proposal.getStatus()));
            return messageFactory.createMessageForProposalStatus(context.getChatId(), proposal);
        } catch (IOException e) {
            return messageFactory.createPhotoErrorMessage(context.getChatId());
        }
    }
}
