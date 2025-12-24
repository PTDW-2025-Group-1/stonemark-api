package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.SharedCallbackData;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.LocationRequest;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.utils.TextTemplateParser;
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConfirmMarkMatchHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;
    private final TextTemplateParser parser;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getCallbackData() == null || !input.getCallbackData().startsWith(ProposalCallbackData.CONFIRM_MARK_PREFIX)) {
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(parser.parse(Messages.CONFIRM_MARK_MATCH_PROMPT)).build())
                    .build());
        }

        String[] callbackDataParts = input.getCallbackData().split(":");
        if (callbackDataParts.length < 2) {
             return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(parser.parse(Messages.INVALID_SELECTION)).build())
                    .build());
        }

        boolean matches = SharedCallbackData.CONFIRM_YES.equalsIgnoreCase(callbackDataParts[1]);

        if (matches) {
            Long markId = Long.valueOf(callbackDataParts[2]);
            proposalFlowService.selectMark(context.getProposal().getId(), markId);
            context.setCurrentState(ConversationState.AWAITING_LOCATION);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(LocationRequest.builder().
                            messageNode(parser.parse(Messages.LOCATION_REQUEST_MESSAGE)).build())
                    .build());
        } else {
            context.setCurrentState(ConversationState.AWAITING_NEW_MARK_DETAILS);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(parser.parse(Messages.PROVIDE_NEW_MARK_DETAILS_PROMPT)).build())
                    .build());
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_MARK_CONFIRMATION;
    }
}
