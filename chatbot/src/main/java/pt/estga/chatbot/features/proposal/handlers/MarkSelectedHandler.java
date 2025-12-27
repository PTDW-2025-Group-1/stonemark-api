package pt.estga.chatbot.features.proposal.handlers;

import org.springframework.stereotype.Component;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;

@Component
public class MarkSelectedHandler implements ConversationStateHandler {
    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.MARK_SELECTED;
    }

    @Override
    public boolean isAutomatic() {
        return true;
    }
}
