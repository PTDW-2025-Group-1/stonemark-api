package pt.estga.chatbot.services;

import org.springframework.stereotype.Service;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.models.BotInput;
import pt.estga.chatbot.models.BotResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConversationDispatcher {

    private final Map<ConversationState, ConversationStateHandler> handlers;
    private final ConversationFlowManager proposalFlow;
    private final ResponseFactory responseFactory;

    public ConversationDispatcher(List<ConversationStateHandler> handlerList, ConversationFlowManager proposalFlow, ResponseFactory responseFactory) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(ConversationStateHandler::canHandle, Function.identity()));
        this.proposalFlow = proposalFlow;
        this.responseFactory = responseFactory;
    }

    public List<BotResponse> dispatch(ChatbotContext context, BotInput input) {
        ConversationState currentState = context.getCurrentState();
        ConversationStateHandler handler = handlers.get(currentState);

        if (handler == null) {
            return responseFactory.createErrorResponse(context);
        }

        // Execute the handler for the current state.
        HandlerOutcome outcome = handler.handle(context, input);

        // Handle re-dispatch outcome
        if (outcome == HandlerOutcome.RE_DISPATCH) {
            return dispatch(context, input);
        }

        // Determine the next state.
        ConversationState nextState = proposalFlow.getNextState(context, currentState, outcome);
        context.setCurrentState(nextState);

        // Generate a response for the NEW state.
        List<BotResponse> responses = new ArrayList<>(responseFactory.createResponse(context, outcome, input));

        // If the next state is automatic, dispatch it immediately and accumulate the responses.
        ConversationStateHandler nextHandler = handlers.get(nextState);
        if (nextHandler != null && nextHandler.isAutomatic()) {
            // Create a new input that preserves the user and platform info, but is otherwise empty.
            BotInput nextInput = BotInput.builder()
                    .userId(input.getUserId())
                    .platform(input.getPlatform())
                    .build();
            responses.addAll(dispatch(context, nextInput));
        }

        return responses;
    }
}
