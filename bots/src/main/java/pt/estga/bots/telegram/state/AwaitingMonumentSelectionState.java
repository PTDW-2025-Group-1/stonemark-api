package pt.estga.bots.telegram.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import pt.estga.bots.telegram.context.ConversationContext;
import pt.estga.bots.telegram.message.TelegramBotMessageFactory;
import pt.estga.bots.telegram.state.factory.StateFactory;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MonumentService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AwaitingMonumentSelectionState implements ConversationState {

    private static final double MONUMENT_SEARCH_RADIUS_KM = 0.1;

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final TelegramBotMessageFactory messageFactory;
    private final StateFactory stateFactory;
    private final MonumentService monumentService;

    @Override
    public ProposalStatus getAssociatedStatus() {
        return ProposalStatus.AWAITING_MONUMENT_SELECTION;
    }

    @Override
    public BotApiMethod<?> onEnter(ConversationContext context) {
        MarkOccurrenceProposal proposal = context.getProposal();
        List<Monument> monuments = monumentService.findByCoordinatesInRange(
                proposal.getLatitude(),
                proposal.getLongitude(),
                MONUMENT_SEARCH_RADIUS_KM
        );

        if (monuments.isEmpty()) {
            proposal = proposalFlowService.requestNewMonument(proposal.getId());
            context.setProposal(proposal);
            context.setState(stateFactory.createState(proposal.getStatus()));
            return messageFactory.createMessageForProposalStatus(context.getChatId(), proposal);
        } else {
            String monumentIds = monuments.stream()
                    .map(monument -> monument.getId().toString())
                    .collect(Collectors.joining(","));
            proposal.setSuggestedMonumentIds(monumentIds);
            context.setProposal(proposal);
            return messageFactory.createMonumentSelectionMessage(context.getChatId(), monuments);
        }
    }

    @Override
    public BotApiMethod<?> handleTextMessage(ConversationContext context, String messageText) {
        MarkOccurrenceProposal proposal;
        if ("new".equalsIgnoreCase(messageText)) {
            proposal = proposalFlowService.requestNewMonument(context.getProposal().getId());
        } else {
            try {
                Long monumentId = Long.parseLong(messageText);
                proposal = proposalFlowService.selectMonument(context.getProposalId(), monumentId);
            } catch (NumberFormatException e) {
                return messageFactory.createInvalidInputMessage(context.getChatId());
            }
        }
        context.setProposal(proposal);
        context.setState(stateFactory.createState(proposal.getStatus()));
        return messageFactory.createMessageForProposalStatus(context.getChatId(), proposal);
    }
}
