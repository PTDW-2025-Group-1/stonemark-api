package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.SharedCallbackData;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConfirmMonumentHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;
    private final SubmissionLoopHandler submissionLoopHandler;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getCallbackData() == null || !input.getCallbackData().startsWith(ProposalCallbackData.CONFIRM_MONUMENT_PREFIX)) {
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title("Please confirm if the monument matches by clicking Yes or No.").build())
                    .build());
        }

        Long proposalId = context.getProposal().getId();
        String[] callbackDataParts = input.getCallbackData().split(":");
        
        if (callbackDataParts.length < 2) {
             return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title("Invalid selection. Please try again.").build())
                    .build());
        }

        boolean confirmed = SharedCallbackData.CONFIRM_YES.equalsIgnoreCase(callbackDataParts[1]);

        if (confirmed) {
            Long monumentId = Long.valueOf(callbackDataParts[2]);
            MarkOccurrenceProposal updatedProposal = proposalFlowService.selectMonument(proposalId, monumentId);
            context.setProposal(updatedProposal);
            context.setCurrentState(ConversationState.SUBMISSION_LOOP_OPTIONS);
            return submissionLoopHandler.handle(context, BotInput.builder().build());
        } else {
            context.setCurrentState(ConversationState.AWAITING_NEW_MONUMENT_NAME);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title("Understood. Please provide the name of the new monument.").build())
                    .build());
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_MONUMENT_CONFIRMATION;
    }
}
