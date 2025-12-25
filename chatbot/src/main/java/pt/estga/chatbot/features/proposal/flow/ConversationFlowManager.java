package pt.estga.chatbot.features.proposal.flow;

import org.springframework.stereotype.Component;
import pt.estga.chatbot.context.ConversationContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.CoreState;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.context.VerificationState;

import java.util.List;
import java.util.Map;

import static pt.estga.chatbot.context.HandlerOutcome.*;

@Component
public class ConversationFlowManager {

    private static final Map<ConversationState, ConversationState> SUCCESS_TRANSITIONS = Map.ofEntries(
            Map.entry(CoreState.START, ProposalState.AWAITING_PROPOSAL_ACTION),
            Map.entry(ProposalState.AWAITING_PROPOSAL_ACTION, ProposalState.WAITING_FOR_PHOTO),
            Map.entry(ProposalState.WAITING_FOR_PHOTO, ProposalState.AWAITING_LOCATION),
            Map.entry(ProposalState.AWAITING_LOCATION, ProposalState.LOOP_OPTIONS),
            Map.entry(ProposalState.AWAITING_MARK_SELECTION, ProposalState.AWAITING_MONUMENT_SUGGESTIONS),
            Map.entry(ProposalState.WAITING_FOR_MARK_CONFIRMATION, ProposalState.AWAITING_MONUMENT_SUGGESTIONS),
            Map.entry(ProposalState.AWAITING_NEW_MARK_DETAILS, ProposalState.AWAITING_MONUMENT_SUGGESTIONS),
            Map.entry(ProposalState.WAITING_FOR_MONUMENT_CONFIRMATION, ProposalState.SUBMISSION_LOOP_OPTIONS),
            Map.entry(ProposalState.AWAITING_NEW_MONUMENT_NAME, ProposalState.SUBMISSION_LOOP_OPTIONS),
            Map.entry(ProposalState.AWAITING_DISCARD_CONFIRMATION, ProposalState.SUBMISSION_LOOP_OPTIONS),
            Map.entry(ProposalState.SUBMISSION_LOOP_OPTIONS, ProposalState.AWAITING_NOTES),
            Map.entry(ProposalState.AWAITING_NOTES, ProposalState.SUBMITTED),
            Map.entry(ProposalState.SUBMITTED, CoreState.START),
            
            // Verification Flow
            Map.entry(VerificationState.AWAITING_CONTACT, VerificationState.AWAITING_VERIFICATION_CODE),
            Map.entry(VerificationState.AWAITING_VERIFICATION_CODE, CoreState.START)
    );

    public ConversationState getNextState(ConversationContext context, ConversationState currentState, HandlerOutcome outcome) {
        // Handle branching from START state (MainMenuHandler)
        if (currentState == CoreState.START) {
            if (outcome == START_NEW) return ProposalState.WAITING_FOR_PHOTO;
            if (outcome == START_VERIFICATION) return VerificationState.AWAITING_VERIFICATION_METHOD;
        }

        // Handle branching from AWAITING_VERIFICATION_METHOD
        if (currentState == VerificationState.AWAITING_VERIFICATION_METHOD) {
            if (outcome == VERIFY_WITH_CODE) return VerificationState.AWAITING_VERIFICATION_CODE;
            if (outcome == VERIFY_WITH_PHONE) return VerificationState.AWAITING_CONTACT;
        }

        // Handle branching from AWAITING_PROPOSAL_ACTION
        if (currentState == ProposalState.AWAITING_PROPOSAL_ACTION) {
            if (outcome == CONTINUE) return ProposalState.LOOP_OPTIONS;
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
            List<String> suggestions = context.getSuggestedMarkIds();
            if (suggestions == null || suggestions.isEmpty()) {
                return ProposalState.AWAITING_NEW_MARK_DETAILS;
            } else if (suggestions.size() == 1) {
                return ProposalState.WAITING_FOR_MARK_CONFIRMATION;
            } else {
                return ProposalState.AWAITING_MARK_SELECTION;
            }
        }
        
        // Handle branching from mark confirmation
        if (currentState == ProposalState.WAITING_FOR_MARK_CONFIRMATION && outcome == REJECTED) {
            return ProposalState.AWAITING_NEW_MARK_DETAILS;
        }

        // Handle branching from mark selection
        if (currentState == ProposalState.AWAITING_MARK_SELECTION && outcome == PROPOSE_NEW) {
            return ProposalState.AWAITING_NEW_MARK_DETAILS;
        }

        // Handle branching after monument suggestion
        if (currentState == ProposalState.AWAITING_MONUMENT_SUGGESTIONS && outcome == SUCCESS) {
            List<String> suggestions = context.getSuggestedMonumentIds();
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
