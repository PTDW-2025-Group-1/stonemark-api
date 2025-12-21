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
        MarkOccurrenceProposal existingProposal = context.getProposal();
        if (existingProposal == null) {
            throw new IllegalStateException("Proposal not found in context for AwaitingMonumentNameState.");
        }

        // The location is already stored in the proposal from the previous step
        MarkOccurrenceProposal proposal = proposalFlowService.proposeMonument(
                context.getProposalId(),
                messageText, // The monument name from the user
                existingProposal.getLatitude(), // Latitude is already set
                existingProposal.getLongitude()  // Longitude is already set
        );
        context.setProposal(proposal);
        context.setState(stateFactory.createState(proposal.getStatus()));
        return messageFactory.createMessageForProposalStatus(context.getChatId(), proposal);
    }
}
