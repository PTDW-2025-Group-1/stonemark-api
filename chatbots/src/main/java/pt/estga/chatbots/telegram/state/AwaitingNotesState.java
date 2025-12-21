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

@Component
@RequiredArgsConstructor
public class AwaitingNotesState implements ConversationState {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final TelegramBotMessageFactory messageFactory;
    private final StateFactory stateFactory;

    @Override
    public ProposalStatus getAssociatedStatus() {
        return ProposalStatus.AWAITING_NOTES;
    }

    @Override
    public BotApiMethod<?> handleTextMessage(ConversationContext context, String messageText) {
        MarkOccurrenceProposal proposal = proposalFlowService.addNotesToProposal(context.getProposalId(), messageText);
        context.setProposal(proposal);
        context.setState(stateFactory.createState(proposal.getStatus()));
        return messageFactory.createMessageForProposalStatus(context.getChatId(), proposal);
    }

    @Override
    public BotApiMethod<?> handleSkipCommand(ConversationContext context) {
        // User skipped adding notes
        MarkOccurrenceProposal proposal = proposalFlowService.addNotesToProposal(context.getProposalId(), null);
        context.setProposal(proposal);
        context.setState(stateFactory.createState(proposal.getStatus()));
        return messageFactory.createMessageForProposalStatus(context.getChatId(), proposal);
    }
}
