package pt.estga.chatbot.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.context.*;
import pt.estga.chatbot.features.proposal.IncompleteSubmissionResolver;
import pt.estga.chatbot.features.proposal.ProposalCallbackData;
import pt.estga.chatbot.features.verification.VerificationCallbackData;
import pt.estga.chatbot.models.BotInput;

@Component
@RequiredArgsConstructor
public class MainMenuHandler implements ConversationStateHandler {

    private final IncompleteSubmissionResolver incompleteSubmissionResolver;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null) {
            return HandlerOutcome.FAILURE;
        }

        if (callbackData.equals(ProposalCallbackData.START_SUBMISSION)) {
            if (incompleteSubmissionResolver.hasIncompleteSubmission(Long.valueOf(input.getUserId()))) {
                return HandlerOutcome.CONTINUE;
            }
            return HandlerOutcome.START_NEW;
        }

        if (callbackData.equals(VerificationCallbackData.START_VERIFICATION)) {
            return HandlerOutcome.START_VERIFICATION;
        }

        return HandlerOutcome.FAILURE;
    }

    @Override
    public ConversationState canHandle() {
        return CoreState.MAIN_MENU;
    }
}
