package pt.estga.chatbots.context;

import pt.estga.chatbots.models.BotInput;

/**
 * Defines a handler for a specific state in the conversation.
 * A handler is responsible for executing business logic for its state
 * and reporting the outcome. It does NOT decide the next state.
 */
public interface ConversationStateHandler {

    /**
     * Handles the user input for the current conversation state.
     *
     * @param context The current conversation context.
     * @param input The user's input.
     * @return The outcome of the handling process.
     */
    HandlerOutcome handle(ConversationContext context, BotInput input);

    /**
     * @return The specific {@link ConversationState} that this handler is responsible for.
     */
    ConversationState canHandle();
}
