package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.proposal.service.MarkProcessorService;
import pt.estga.chatbots.core.proposal.service.ProposalNavigationService;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Button;
import pt.estga.chatbots.core.shared.models.ui.LocationRequest;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.services.UiTextService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoopOptionsHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;
    private final MarkProcessorService markProcessorService;
    private final ProposalNavigationService navigationService;
    private final UiTextService textService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        // If there's no specific action, just show the main options menu
        if (callbackData == null) {
            return showOptions(context.getProposal());
        }

        switch (callbackData) {
            case ProposalCallbackData.LOOP_REDO_LOCATION:
                context.setCurrentState(ConversationState.AWAITING_LOCATION);
                return Collections.singletonList(BotResponse.builder()
                        .uiComponent(LocationRequest.builder()
                                .messageNode(textService.get(Messages.REQUEST_LOCATION_PROMPT)).build())
                        .build());

            case ProposalCallbackData.LOOP_REDO_IMAGE_UPLOAD:
                context.setCurrentState(ConversationState.WAITING_FOR_PHOTO);
                return Collections.singletonList(BotResponse.builder().uiComponent(Menu.builder()
                        .titleNode(textService.get(Messages.REQUEST_PHOTO_PROMPT)).build()).build());

            case ProposalCallbackData.LOOP_CONTINUE:
                // The "Continue" button now uses the central navigation logic
                // This will correctly ask for missing info before proceeding
                List<BotResponse> navResponses = navigationService.navigate(context);
                if (navResponses != null) {
                    return navResponses;
                }
                
                // If navigate() returns null, it means all info is present and we can proceed
                try {
                    MarkOccurrenceProposal updatedProposal = proposalFlowService.analyzePhoto(context.getProposal().getId());
                    context.setProposal(updatedProposal);
                    List<BotResponse> suggestionResponses = markProcessorService.processMarkSuggestions(context, updatedProposal);

                    // If no suggestions are found, guide the user to create a new mark
                    if (suggestionResponses.isEmpty()) {
                        context.setCurrentState(ConversationState.AWAITING_NEW_MARK_DETAILS);
                        return Collections.singletonList(BotResponse.builder()
                                .uiComponent(Menu.builder().titleNode(textService.get(Messages.PROVIDE_NEW_MARK_DETAILS_PROMPT)).build())
                                .build());
                    }

                    return suggestionResponses;
                } catch (IOException e) {
                    return Collections.singletonList(BotResponse.builder()
                            .uiComponent(Menu.builder().titleNode(textService.get(Messages.ERROR_PROCESSING_SUBMISSION)).build())
                            .build());
                }

            default:
                return showOptions(context.getProposal());
        }
    }

    private List<BotResponse> showOptions(MarkOccurrenceProposal proposal) {
        Menu menu = Menu.builder()
                .titleNode(textService.get(Messages.LOOP_OPTIONS_TITLE))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(Messages.CHANGE_LOCATION_BTN)).callbackData(ProposalCallbackData.LOOP_REDO_LOCATION).build()),
                        List.of(Button.builder().textNode(textService.get(Messages.CHANGE_PHOTO_BTN)).callbackData(ProposalCallbackData.LOOP_REDO_IMAGE_UPLOAD).build()),
                        List.of(Button.builder().textNode(textService.get(Messages.CONTINUE_BTN)).callbackData(ProposalCallbackData.LOOP_CONTINUE).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.LOOP_OPTIONS;
    }
}
