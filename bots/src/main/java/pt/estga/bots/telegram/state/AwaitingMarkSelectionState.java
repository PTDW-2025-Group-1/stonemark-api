package pt.estga.bots.telegram.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import pt.estga.bots.telegram.context.ConversationContext;
import pt.estga.bots.telegram.message.TelegramBotMessageFactory;
import pt.estga.bots.telegram.state.factory.StateFactory;
import pt.estga.content.services.MarkService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
public class AwaitingMarkSelectionState implements ConversationState {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final TelegramBotMessageFactory messageFactory;
    private final StateFactory stateFactory;
    private final MarkService markService;

    @Override
    public ProposalStatus getAssociatedStatus() {
        return ProposalStatus.AWAITING_MARK_SELECTION;
    }

    @Override
    public BotApiMethod<?> handleTextMessage(ConversationContext context, String messageText) {
        MarkOccurrenceProposal proposal;
        if ("new".equalsIgnoreCase(messageText)) {
            proposal = proposalFlowService.requestNewMark(context.getProposal().getId());
        } else {
            try {
                Long markId = Long.parseLong(messageText);
                proposal = proposalFlowService.selectMark(context.getProposalId(), markId);
            } catch (NumberFormatException e) {
                return messageFactory.createInvalidInputMessage(context.getChatId());
            }
        }
        context.setProposal(proposal);
        context.setState(stateFactory.createState(proposal.getStatus()));
        return messageFactory.createMessageForProposalStatus(context.getChatId(), proposal);
    }
}
