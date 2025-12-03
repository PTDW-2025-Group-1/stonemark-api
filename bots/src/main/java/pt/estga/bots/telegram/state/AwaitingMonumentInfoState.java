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
import pt.estga.shared.models.Location;

@Component
@RequiredArgsConstructor
public class AwaitingMonumentInfoState implements ConversationState {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final TelegramBotMessageFactory messageFactory;
    private final StateFactory stateFactory;

    @Override
    public ProposalStatus getAssociatedStatus() {
        return ProposalStatus.AWAITING_MONUMENT_INFO;
    }

    @Override
    public BotApiMethod<?> handleLocationSubmission(ConversationContext context, Location location) {
        // Todo: get a proper name from user or API
        MarkOccurrenceProposal proposal = proposalFlowService.proposeMonument(context.getProposalId(), "New Monument", location.getLatitude(), location.getLongitude());
        context.setState(stateFactory.createState(proposal.getStatus()));
        return messageFactory.createMessageForProposalStatus(context.getChatId(), proposal);
    }
}
