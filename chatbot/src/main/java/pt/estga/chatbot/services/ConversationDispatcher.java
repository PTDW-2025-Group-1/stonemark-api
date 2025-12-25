package pt.estga.chatbot.services;

import org.springframework.stereotype.Service;
import pt.estga.chatbot.features.proposal.flow.ConversationFlowManager;
import pt.estga.chatbot.context.ConversationContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.chatbot.models.BotResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConversationDispatcher {

    private final Map<ConversationState, ConversationStateHandler> handlers;
    private final ConversationFlowManager proposalFlow;
    private final ResponseFactory responseFactory;

    // States that are "headless" and should be executed immediately without user input.
    private static final Set<ConversationState> IMMEDIATE_STATES = Set.of(
            ProposalState.AWAITING_PHOTO_ANALYSIS,
            ProposalState.AWAITING_MONUMENT_SUGGESTIONS
    );

    public ConversationDispatcher(List<ConversationStateHandler> handlerList, ConversationFlowManager proposalFlow, ResponseFactory responseFactory) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(ConversationStateHandler::canHandle, Function.identity()));
        this.proposalFlow = proposalFlow;
        this.responseFactory = responseFactory;
    }

    public List<BotResponse> dispatch(ConversationContext context, BotInput input) {
        ConversationState currentState = context.getCurrentState();
        ConversationStateHandler handler = handlers.get(currentState);

        if (handler == null) {
            return responseFactory.createErrorResponse(context);
        }

        // 1. Execute the handler for the current state.
        HandlerOutcome outcome = handler.handle(context, input);

        // 2. Ask the ConversationFlowManager for the next state based on the outcome.
        ConversationState nextState = proposalFlow.getNextState(context, currentState, outcome);
        context.setCurrentState(nextState);

        // 3. If the next state is an immediate one, dispatch it recursively.
        if (IMMEDIATE_STATES.contains(nextState)) {
            // Pass an empty input to the next handler in the chain.
            return dispatch(context, BotInput.builder().build());
        }

        // 4. Generate a response for the user for the new state.
        return responseFactory.createResponse(context, outcome, input);
    }
}
