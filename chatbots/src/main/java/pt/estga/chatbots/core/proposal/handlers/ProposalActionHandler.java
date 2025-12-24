package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.utils.TextTemplateParser;
import pt.estga.proposals.services.MarkOccurrenceProposalService;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProposalActionHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalService proposalService;
    private final LoopOptionsHandler loopOptionsHandler;
    private final TextTemplateParser parser;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null) {
            return null;
        }

        if (callbackData.equals(ProposalCallbackData.CONTINUE_PROPOSAL)) {
            context.setCurrentState(ConversationState.LOOP_OPTIONS);
            return loopOptionsHandler.handle(context, BotInput.builder().build());
        }

        if (callbackData.equals(ProposalCallbackData.DELETE_AND_START_NEW)) {
            proposalService.delete(context.getProposal());
            context.setProposal(null);
            context.setCurrentState(ConversationState.WAITING_FOR_PHOTO);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(parser.parse("Your previous submission has been deleted. Please send a clear photo of the mark to start a new one.")).build())
                    .build());
        }

        return null;
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_PROPOSAL_ACTION;
    }
}
