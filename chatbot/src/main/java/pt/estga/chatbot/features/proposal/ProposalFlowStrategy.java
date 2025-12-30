package pt.estga.chatbot.features.proposal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.CoreState;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.services.FlowStrategy;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.services.MarkOccurrenceProposalChatbotFlowService;

import java.util.List;
import java.util.Map;

import static pt.estga.chatbot.context.HandlerOutcome.*;

@Component
@RequiredArgsConstructor
public class ProposalFlowStrategy implements FlowStrategy {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;
    private final IncompleteSubmissionResolver incompleteSubmissionResolver;

    private static final Map<ConversationState, ConversationState> SUCCESS_TRANSITIONS = Map.ofEntries(
            Map.entry(ProposalState.AWAITING_PROPOSAL_ACTION, ProposalState.WAITING_FOR_PHOTO),
            Map.entry(ProposalState.WAITING_FOR_PHOTO, ProposalState.AWAITING_LOCATION),
            Map.entry(ProposalState.AWAITING_LOCATION, ProposalState.LOOP_OPTIONS),
            Map.entry(ProposalState.AWAITING_MARK_SELECTION, ProposalState.MARK_SELECTED),
            Map.entry(ProposalState.WAITING_FOR_MARK_CONFIRMATION, ProposalState.MARK_SELECTED),
            Map.entry(ProposalState.MARK_SELECTED, ProposalState.AWAITING_MONUMENT_SUGGESTIONS),
            Map.entry(ProposalState.WAITING_FOR_MONUMENT_CONFIRMATION, ProposalState.SUBMISSION_LOOP_OPTIONS),
            Map.entry(ProposalState.AWAITING_NEW_MONUMENT_NAME, ProposalState.SUBMISSION_LOOP_OPTIONS),
            Map.entry(ProposalState.AWAITING_DISCARD_CONFIRMATION, ProposalState.SUBMISSION_LOOP_OPTIONS),
            Map.entry(ProposalState.SUBMISSION_LOOP_OPTIONS, ProposalState.AWAITING_NOTES),
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
            if (incompleteSubmissionResolver.hasIncompleteSubmission(context.getDomainUserId())) {
                return ProposalState.AWAITING_PROPOSAL_ACTION;
            }
            return ProposalState.WAITING_FOR_PHOTO;
        }

        // Handle branching from AWAITING_PROPOSAL_ACTION
        if (currentState == ProposalState.AWAITING_PROPOSAL_ACTION) {
            if (outcome == CONTINUE) {
                MarkOccurrenceProposal proposal = proposalFlowService.getProposal(context.getProposalContext().getProposalId());
                if (proposal.getOriginalMediaFile() == null) {
                    return ProposalState.WAITING_FOR_PHOTO;
                }
                if (proposal.getLatitude() == null || proposal.getLongitude() == null) {
                    return ProposalState.AWAITING_LOCATION;
                }
                return ProposalState.LOOP_OPTIONS;
            }
            if (outcome == DISCARD_CONFIRMED) return ProposalState.WAITING_FOR_PHOTO;
        }

        // Handle branching from LOOP_OPTIONS
        if (currentState == ProposalState.LOOP_OPTIONS) {
            switch (outcome) {
                case CHANGE_LOCATION: return ProposalState.AWAITING_LOCATION;
                case CHANGE_PHOTO: return ProposalState.WAITING_FOR_PHOTO;
                case CONTINUE: return ProposalState.AWAITING_PHOTO_ANALYSIS;
            }
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
                return ProposalState.AWAITING_NEW_MONUMENT_NAME;
            } else {
                return ProposalState.WAITING_FOR_MONUMENT_CONFIRMATION;
            }
        }

        // Handle branching from monument confirmation
        if (currentState == ProposalState.WAITING_FOR_MONUMENT_CONFIRMATION && outcome == REJECTED) {
            return ProposalState.AWAITING_NEW_MONUMENT_NAME;
        }

        // Handle branching from submission loop
        if (currentState == ProposalState.SUBMISSION_LOOP_OPTIONS) {
            if (outcome == DISCARD) return ProposalState.AWAITING_DISCARD_CONFIRMATION;
            if (outcome == CONTINUE) return ProposalState.AWAITING_NOTES;
        }

        // Handle branching from discard confirmation
        if (currentState == ProposalState.AWAITING_DISCARD_CONFIRMATION && outcome == DISCARD_CONFIRMED) {
            return CoreState.START;
        }

        if (outcome == SUCCESS) {
            return SUCCESS_TRANSITIONS.getOrDefault(currentState, CoreState.START);
        }

        return currentState;
    }
}
