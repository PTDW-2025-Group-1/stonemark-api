package pt.estga.bots.telegram.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import pt.estga.bots.telegram.context.ConversationContext;
import pt.estga.bots.telegram.message.TelegramBotMessageFactory;
import pt.estga.bots.telegram.state.factory.StateFactory;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
public class AwaitingMarkInfoState implements ConversationState {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final TelegramBotMessageFactory messageFactory;
    private final StateFactory stateFactory;

    @Override
    public ProposalStatus getAssociatedStatus() {
        return ProposalStatus.AWAITING_MARK_INFO;
    }

    @Override
    public BotApiMethod<?> handleTextMessage(ConversationContext context, String messageText) {
        String[] parts = messageText.split("\\n", 2);
        if (parts.length < 2) {
            return messageFactory.createInvalidMarkDetailsFormatMessage(context.getChatId());
        }
        String title = parts[0];
        String description = parts[1];

        MarkOccurrenceProposal proposal = proposalFlowService.proposeMark(context.getProposalId(), title, description);
        context.setState(stateFactory.createState(proposal.getStatus()));
        return messageFactory.createMessageForProposalStatus(context.getChatId(), proposal);
    }
}
