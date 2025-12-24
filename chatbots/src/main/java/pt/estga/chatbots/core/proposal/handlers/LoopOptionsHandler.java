package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.proposal.service.MarkProcessorService;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Button;
import pt.estga.chatbots.core.shared.models.ui.LocationRequest;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.utils.TextTemplateParser;
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
    private final TextTemplateParser parser;

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
                return Collections.singletonList(BotResponse.builder()
                        .uiComponent(LocationRequest.builder()
                                .messageNode(parser.parse(Messages.SEND_NEW_LOCATION_PROMPT)).build())
                        .build());

            case ProposalCallbackData.LOOP_REDO_IMAGE_UPLOAD:
                context.setCurrentState(ConversationState.AWAITING_REUPLOAD_PHOTO);
                return Collections.singletonList(BotResponse.builder().uiComponent(Menu.builder()
                        .titleNode(parser.parse(Messages.UPLOAD_NEW_IMAGE_PROMPT)).build()).build());

            case ProposalCallbackData.LOOP_CONTINUE:
                try {
                    MarkOccurrenceProposal updatedProposal = proposalFlowService.analyzePhoto(proposal.getId());
                    context.setProposal(updatedProposal);
                    return markProcessorService.processMarkSuggestions(context, updatedProposal);
                } catch (IOException e) {
                    return Collections.singletonList(BotResponse.builder()
                            .uiComponent(Menu.builder().titleNode(parser.parse(Messages.ERROR_PROCESSING_SUBMISSION)).build())
                            .build());
                }

            default:
                return showOptions(proposal);
        }
    }

    private List<BotResponse> showOptions(MarkOccurrenceProposal proposal) {
        Menu menu = Menu.builder()
                .titleNode(parser.parse(Messages.LOOP_OPTIONS_TITLE))
                .buttons(List.of(
                        List.of(Button.builder().textNode(parser.parse(Messages.CHANGE_LOCATION_BTN)).callbackData(ProposalCallbackData.LOOP_REDO_LOCATION).build()),
                        List.of(Button.builder().textNode(parser.parse(Messages.CHANGE_PHOTO_BTN)).callbackData(ProposalCallbackData.LOOP_REDO_IMAGE_UPLOAD).build()),
                        List.of(Button.builder().textNode(parser.parse(Messages.CONTINUE_BTN)).callbackData(ProposalCallbackData.LOOP_CONTINUE).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.LOOP_OPTIONS;
    }
}
