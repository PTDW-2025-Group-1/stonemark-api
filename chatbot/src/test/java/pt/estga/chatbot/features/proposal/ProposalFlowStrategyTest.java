package pt.estga.chatbot.features.proposal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProposalFlowStrategyTest {

    private ProposalFlowStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ProposalFlowStrategy();
    }

    @Test
    void getNextState_ShouldReturnAwaitingNotes_WhenNoMonumentsSuggested() {
        ChatbotContext context = new ChatbotContext();
        context.getProposalContext().setSuggestedMonumentIds(Collections.emptyList());

        ConversationState nextState = strategy.getNextState(
                context,
                ProposalState.AWAITING_MONUMENT_SUGGESTIONS,
                HandlerOutcome.SUCCESS
        );

        assertEquals(ProposalState.AWAITING_NOTES, nextState);
    }

    @Test
    void getNextState_ShouldReturnWaitingForMonumentConfirmation_WhenOneMonumentSuggested() {
        ChatbotContext context = new ChatbotContext();
        context.getProposalContext().setSuggestedMonumentIds(List.of("1"));

        ConversationState nextState = strategy.getNextState(
                context,
                ProposalState.AWAITING_MONUMENT_SUGGESTIONS,
                HandlerOutcome.SUCCESS
        );

        assertEquals(ProposalState.WAITING_FOR_MONUMENT_CONFIRMATION, nextState);
    }

    @Test
    void getNextState_ShouldReturnAwaitingMonumentSelection_WhenMultipleMonumentsSuggested() {
        ChatbotContext context = new ChatbotContext();
        context.getProposalContext().setSuggestedMonumentIds(List.of("1", "2"));

        ConversationState nextState = strategy.getNextState(
                context,
                ProposalState.AWAITING_MONUMENT_SUGGESTIONS,
                HandlerOutcome.SUCCESS
        );

        assertEquals(ProposalState.AWAITING_MONUMENT_SELECTION, nextState);
    }
}
