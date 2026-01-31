package pt.estga.chatbot.features.proposal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.CoreState;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.services.FlowStrategy;

import java.util.List;
import java.util.Map;

import static pt.estga.chatbot.context.HandlerOutcome.*;

@Component
@RequiredArgsConstructor
public class ProposalFlowStrategy implements FlowStrategy {

    private static final Map<ConversationState, ConversationState> SUCCESS_TRANSITIONS = Map.ofEntries(
            Map.entry(ProposalState.WAITING_FOR_PHOTO, ProposalState.AWAITING_LOCATION),
            Map.entry(ProposalState.AWAITING_LOCATION, ProposalState.AWAITING_PHOTO_ANALYSIS),
            Map.entry(ProposalState.AWAITING_MARK_SELECTION, ProposalState.MARK_SELECTED),
            Map.entry(ProposalState.WAITING_FOR_MARK_CONFIRMATION, ProposalState.MARK_SELECTED),
            Map.entry(ProposalState.MARK_SELECTED, ProposalState.AWAITING_MONUMENT_SUGGESTIONS),
            Map.entry(ProposalState.AWAITING_MONUMENT_SELECTION, ProposalState.WAITING_FOR_MONUMENT_CONFIRMATION),
            Map.entry(ProposalState.WAITING_FOR_MONUMENT_CONFIRMATION, ProposalState.AWAITING_NOTES),
            Map.entry(ProposalState.AWAITING_NOTES, ProposalState.SUBMITTED),
            Map.entry(ProposalState.SUBMITTED, CoreState.MAIN_MENU)
    );

    @Override
    public boolean supports(ConversationState state) {
        return state instanceof ProposalState;
    }

    @Override
    public ConversationState getNextState(ChatbotContext context, ConversationState currentState, HandlerOutcome outcome) {
        if (outcome == FAILURE) {
            return currentState;
        }

        if (currentState == ProposalState.PROPOSAL_START) {
            return ProposalState.WAITING_FOR_PHOTO;
        }

        // Handle branching after photo analysis
        if (currentState == ProposalState.AWAITING_PHOTO_ANALYSIS && outcome == SUCCESS) {
            List<String> suggestions = context.getProposalContext().getSuggestedMarkIds();
            if (suggestions == null || suggestions.isEmpty()) {
                return ProposalState.AWAITING_MONUMENT_SUGGESTIONS;
            } else if (suggestions.size() == 1) {
                return ProposalState.WAITING_FOR_MARK_CONFIRMATION;
            } else {
                return ProposalState.AWAITING_MARK_SELECTION;
            }
        }

        // Handle branching from mark confirmation
        if (currentState == ProposalState.WAITING_FOR_MARK_CONFIRMATION && outcome == REJECTED) {
            return ProposalState.AWAITING_MONUMENT_SUGGESTIONS;
        }

        // Handle branching from mark selection
        if (currentState == ProposalState.AWAITING_MARK_SELECTION && outcome == PROPOSE_NEW) {
            return ProposalState.AWAITING_MONUMENT_SUGGESTIONS;
        }

        // Handle branching after monument suggestion
        if (currentState == ProposalState.AWAITING_MONUMENT_SUGGESTIONS && outcome == SUCCESS) {
            List<String> suggestions = context.getProposalContext().getSuggestedMonumentIds();
            if (suggestions == null || suggestions.isEmpty()) {
                return ProposalState.AWAITING_NOTES;
            } else if (suggestions.size() == 1) {
                return ProposalState.WAITING_FOR_MONUMENT_CONFIRMATION;
            } else {
                return ProposalState.AWAITING_MONUMENT_SELECTION;
            }
        }

        // Handle branching from monument confirmation
        if (currentState == ProposalState.WAITING_FOR_MONUMENT_CONFIRMATION && outcome == REJECTED) {
            return ProposalState.AWAITING_NOTES;
        }

        if (outcome == SUCCESS) {
            return SUCCESS_TRANSITIONS.getOrDefault(currentState, CoreState.START);
        }

        return currentState;
    }
}
