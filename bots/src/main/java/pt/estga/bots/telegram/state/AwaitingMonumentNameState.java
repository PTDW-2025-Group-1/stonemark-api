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
public class AwaitingMonumentNameState implements ConversationState {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final TelegramBotMessageFactory messageFactory;
    private final StateFactory stateFactory;

    @Override
    public ProposalStatus getAssociatedStatus() {
        return ProposalStatus.AWAITING_MONUMENT_NAME;
    }

    @Override
    public BotApiMethod<?> handleTextMessage(ConversationContext context, String messageText) {
        // The location is already stored in the proposal from the previous step
        MarkOccurrenceProposal proposal = proposalFlowService.proposeMonument(
                context.getProposalId(),
                messageText, // The monument name from the user
                null, // Latitude is already set
                null  // Longitude is already set
        );
        context.setState(stateFactory.createState(proposal.getStatus()));
        return messageFactory.createMessageForProposalStatus(context.getChatId(), proposal);
    }
}
