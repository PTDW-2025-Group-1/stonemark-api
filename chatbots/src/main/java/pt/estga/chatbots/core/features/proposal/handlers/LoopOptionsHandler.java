package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LoopOptionsHandler implements ConversationStateHandler {

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();
        MarkOccurrenceProposal proposal = context.getProposal();

        if (callbackData == null) {
            return showOptions(proposal);
        }

        switch (callbackData) {
            case CallbackData.LOOP_REDO_LOCATION:
                context.setCurrentState(ConversationState.AWAITING_LOCATION);
                return BotResponse.builder().uiComponent(Menu.builder().title("Please send the new location.").build()).build();

            case CallbackData.LOOP_REDO_IMAGE_UPLOAD:
                context.setCurrentState(ConversationState.WAITING_FOR_PHOTO);
                return BotResponse.builder().uiComponent(Menu.builder().title("Please upload a new image.").build()).build();

            case CallbackData.LOOP_CONTINUE:
                context.setCurrentState(ConversationState.AWAITING_NOTES);
                return BotResponse.builder().uiComponent(Menu.builder().title("Please add any notes for this proposal.").build()).build();

            default:
                return showOptions(proposal);
        }
    }

    private BotResponse showOptions(MarkOccurrenceProposal proposal) {
        Menu menu = Menu.builder()
                .title("What would you like to do next?")
                .buttons(List.of(
                        List.of(Button.builder().text("Reselect Location").callbackData(CallbackData.LOOP_REDO_LOCATION).build()),
                        List.of(Button.builder().text("Redo Image Upload").callbackData(CallbackData.LOOP_REDO_IMAGE_UPLOAD).build()),
                        List.of(Button.builder().text("Continue to Notes").callbackData(CallbackData.LOOP_CONTINUE).build())
                ))
                .build();
        return BotResponse.builder().uiComponent(menu).build();
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.LOOP_OPTIONS;
    }
}
