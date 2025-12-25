package pt.estga.chatbots.core.proposal.flow;

import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.HandlerOutcome;

import java.util.List;
import java.util.Map;

import static pt.estga.chatbots.core.shared.context.ConversationState.*;
import static pt.estga.chatbots.core.shared.context.HandlerOutcome.*;

@Component
public class ProposalFlow {

    private static final Map<ConversationState, ConversationState> SUCCESS_TRANSITIONS = Map.ofEntries(
            Map.entry(START, AWAITING_PROPOSAL_ACTION),
            Map.entry(AWAITING_PROPOSAL_ACTION, WAITING_FOR_PHOTO),
            Map.entry(WAITING_FOR_PHOTO, AWAITING_LOCATION),
            Map.entry(AWAITING_LOCATION, LOOP_OPTIONS),
            Map.entry(AWAITING_MARK_SELECTION, AWAITING_MONUMENT_SUGGESTIONS),
            Map.entry(WAITING_FOR_MARK_CONFIRMATION, AWAITING_MONUMENT_SUGGESTIONS),
            Map.entry(AWAITING_NEW_MARK_DETAILS, AWAITING_MONUMENT_SUGGESTIONS),
            Map.entry(WAITING_FOR_MONUMENT_CONFIRMATION, SUBMISSION_LOOP_OPTIONS),
            Map.entry(AWAITING_NEW_MONUMENT_NAME, SUBMISSION_LOOP_OPTIONS),
            Map.entry(AWAITING_DISCARD_CONFIRMATION, SUBMISSION_LOOP_OPTIONS),
            Map.entry(SUBMISSION_LOOP_OPTIONS, AWAITING_NOTES),
            Map.entry(AWAITING_NOTES, SUBMITTED),
            Map.entry(SUBMITTED, START),
            
            // Verification Flow
            Map.entry(AWAITING_CONTACT, AWAITING_VERIFICATION_CODE),
            Map.entry(AWAITING_VERIFICATION_CODE, START)
    );

    public ConversationState getNextState(ConversationContext context, ConversationState currentState, HandlerOutcome outcome) {
        // Handle branching from START state (MainMenuHandler)
        if (currentState == START) {
            if (outcome == START_NEW) return WAITING_FOR_PHOTO;
            if (outcome == START_VERIFICATION) return AWAITING_VERIFICATION_METHOD;
        }

        // Handle branching from AWAITING_VERIFICATION_METHOD
        if (currentState == AWAITING_VERIFICATION_METHOD) {
            if (outcome == VERIFY_WITH_CODE) return AWAITING_VERIFICATION_CODE;
            if (outcome == VERIFY_WITH_PHONE) return AWAITING_CONTACT;
        }

        // Handle branching from AWAITING_PROPOSAL_ACTION
        if (currentState == AWAITING_PROPOSAL_ACTION) {
            if (outcome == CONTINUE) return LOOP_OPTIONS;
            if (outcome == DISCARD_CONFIRMED) return WAITING_FOR_PHOTO;
        }

        // Handle branching from LOOP_OPTIONS
        if (currentState == LOOP_OPTIONS) {
            switch (outcome) {
                case CHANGE_LOCATION: return AWAITING_LOCATION;
                case CHANGE_PHOTO: return WAITING_FOR_PHOTO;
                case CONTINUE: return AWAITING_PHOTO_ANALYSIS;
            }
        }

        // Handle branching after photo analysis
        if (currentState == AWAITING_PHOTO_ANALYSIS && outcome == SUCCESS) {
            List<String> suggestions = context.getSuggestedMarkIds();
            if (suggestions == null || suggestions.isEmpty()) {
                return AWAITING_NEW_MARK_DETAILS;
            } else if (suggestions.size() == 1) {
                return WAITING_FOR_MARK_CONFIRMATION;
            } else {
                return AWAITING_MARK_SELECTION;
            }
        }
        
        // Handle branching from mark confirmation
        if (currentState == WAITING_FOR_MARK_CONFIRMATION && outcome == REJECTED) {
            return AWAITING_NEW_MARK_DETAILS;
        }

        // Handle branching from mark selection
        if (currentState == AWAITING_MARK_SELECTION && outcome == PROPOSE_NEW) {
            return AWAITING_NEW_MARK_DETAILS;
        }

        // Handle branching after monument suggestion
        if (currentState == AWAITING_MONUMENT_SUGGESTIONS && outcome == SUCCESS) {
            List<String> suggestions = context.getSuggestedMonumentIds();
            if (suggestions == null || suggestions.isEmpty()) {
                return AWAITING_NEW_MONUMENT_NAME;
            } else {
                return WAITING_FOR_MONUMENT_CONFIRMATION;
            }
        }

        // Handle branching from monument confirmation
        if (currentState == WAITING_FOR_MONUMENT_CONFIRMATION && outcome == REJECTED) {
            return AWAITING_NEW_MONUMENT_NAME;
        }

        // Handle branching from submission loop
        if (currentState == SUBMISSION_LOOP_OPTIONS) {
            if (outcome == DISCARD) return AWAITING_DISCARD_CONFIRMATION;
            if (outcome == CONTINUE) return AWAITING_NOTES;
        }

        // Handle branching from discard confirmation
        if (currentState == AWAITING_DISCARD_CONFIRMATION && outcome == DISCARD_CONFIRMED) {
            return START;
        }

        if (outcome == SUCCESS) {
            return SUCCESS_TRANSITIONS.getOrDefault(currentState, START);
        }

        return currentState;
    }
}
