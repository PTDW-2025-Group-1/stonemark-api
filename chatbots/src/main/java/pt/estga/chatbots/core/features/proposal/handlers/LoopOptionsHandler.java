package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.features.proposal.service.MarkProcessorService;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LoopOptionsHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final MarkProcessorService markProcessorService;

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
                context.setCurrentState(ConversationState.AWAITING_REUPLOAD_PHOTO);
                return BotResponse.builder().uiComponent(Menu.builder().title("Please upload a new image.").build()).build();

            case CallbackData.LOOP_CONTINUE:
                try {
                    MarkOccurrenceProposal updatedProposal = proposalFlowService.processSubmission(proposal.getId());
                    context.setProposal(updatedProposal);
                    return markProcessorService.processMarkSuggestions(context, updatedProposal);
                } catch (IOException e) {
                    return BotResponse.builder()
                            .uiComponent(Menu.builder().title("Error processing submission.").build())
                            .build();
                }

            default:
                return showOptions(proposal);
        }
    }

    private BotResponse showOptions(MarkOccurrenceProposal proposal) {
        Menu menu = Menu.builder()
                .title("What would you like to do next?")
                .buttons(List.of(
                        List.of(Button.builder().text("Change Location").callbackData(CallbackData.LOOP_REDO_LOCATION).build()),
                        List.of(Button.builder().text("Change Photo").callbackData(CallbackData.LOOP_REDO_IMAGE_UPLOAD).build()),
                        List.of(Button.builder().text("Continue").callbackData(CallbackData.LOOP_CONTINUE).build())
                ))
                .build();
        return BotResponse.builder().uiComponent(menu).build();
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.LOOP_OPTIONS;
    }
}
