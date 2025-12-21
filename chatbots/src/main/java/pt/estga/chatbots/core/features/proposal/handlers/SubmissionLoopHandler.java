package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.features.common.handlers.StartHandler;
import pt.estga.chatbots.core.features.proposal.service.MarkProcessorService;
import pt.estga.chatbots.core.features.proposal.service.MonumentSuggestionProcessorService;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SubmissionLoopHandler implements ConversationStateHandler {

    private final MarkProcessorService markProcessorService;
    private final MonumentSuggestionProcessorService monumentSuggestionProcessorService;
    private final StartHandler startHandler;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();
        MarkOccurrenceProposal proposal = context.getProposal();

        if (callbackData == null) {
            return showOptions(proposal);
        }

        return switch (callbackData) {
            case CallbackData.SUBMISSION_LOOP_CHANGE_MARK ->
                    markProcessorService.processMarkSuggestions(context, proposal);
            case CallbackData.SUBMISSION_LOOP_CHANGE_MONUMENT ->
                    monumentSuggestionProcessorService.processMonumentSuggestions(context, proposal);
            case CallbackData.SUBMISSION_LOOP_START_OVER -> {
                context.setProposal(null);
                yield startHandler.handle(context, BotInput.builder().text("/start").build());
            }
            case CallbackData.SUBMISSION_LOOP_CONTINUE -> {
                context.setCurrentState(ConversationState.AWAITING_NOTES);
                yield BotResponse.builder().uiComponent(Menu.builder().title("Please add any notes for this proposal.").build()).build();
            }
            default -> showOptions(proposal);
        };
    }

    private BotResponse showOptions(MarkOccurrenceProposal proposal) {
        Menu menu = Menu.builder()
                .title("Review your submission. What would you like to do next?")
                .buttons(List.of(
                        List.of(Button.builder().text("Change Mark").callbackData(CallbackData.SUBMISSION_LOOP_CHANGE_MARK).build()),
                        List.of(Button.builder().text("Change Monument").callbackData(CallbackData.SUBMISSION_LOOP_CHANGE_MONUMENT).build()),
                        List.of(Button.builder().text("Start Over").callbackData(CallbackData.SUBMISSION_LOOP_START_OVER).build()),
                        List.of(Button.builder().text("Continue to Submit").callbackData(CallbackData.SUBMISSION_LOOP_CONTINUE).build())
                ))
                .build();
        return BotResponse.builder().uiComponent(menu).build();
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.SUBMISSION_LOOP_OPTIONS;
    }
}
