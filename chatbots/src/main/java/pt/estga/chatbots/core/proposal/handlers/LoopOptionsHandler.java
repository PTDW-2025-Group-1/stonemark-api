package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.proposal.service.MarkProcessorService;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Button;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LoopOptionsHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final MarkProcessorService markProcessorService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();
        MarkOccurrenceProposal proposal = context.getProposal();

        if (callbackData == null) {
            return showOptions(proposal);
        }

        switch (callbackData) {
            case ProposalCallbackData.LOOP_REDO_LOCATION:
                context.setCurrentState(ConversationState.AWAITING_LOCATION);
                return Collections.singletonList(BotResponse.builder().uiComponent(Menu.builder().title("Please send the new location.").build()).build());

            case ProposalCallbackData.LOOP_REDO_IMAGE_UPLOAD:
                context.setCurrentState(ConversationState.AWAITING_REUPLOAD_PHOTO);
                return Collections.singletonList(BotResponse.builder().uiComponent(Menu.builder().title("Please upload a new image.").build()).build());

            case ProposalCallbackData.LOOP_CONTINUE:
                try {
                    MarkOccurrenceProposal updatedProposal = proposalFlowService.processSubmission(proposal.getId());
                    context.setProposal(updatedProposal);
                    return markProcessorService.processMarkSuggestions(context, updatedProposal);
                } catch (IOException e) {
                    return Collections.singletonList(BotResponse.builder()
                            .uiComponent(Menu.builder().title("Error processing submission.").build())
                            .build());
                }

            default:
                return showOptions(proposal);
        }
    }

    private List<BotResponse> showOptions(MarkOccurrenceProposal proposal) {
        Menu menu = Menu.builder()
                .title("What would you like to do next?")
                .buttons(List.of(
                        List.of(Button.builder().text("Change Location").callbackData(ProposalCallbackData.LOOP_REDO_LOCATION).build()),
                        List.of(Button.builder().text("Change Photo").callbackData(ProposalCallbackData.LOOP_REDO_IMAGE_UPLOAD).build()),
                        List.of(Button.builder().text("Continue").callbackData(ProposalCallbackData.LOOP_CONTINUE).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.LOOP_OPTIONS;
    }
}
