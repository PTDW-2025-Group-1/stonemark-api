package pt.estga.bots.telegram.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import pt.estga.bots.telegram.context.ConversationContext;
import pt.estga.bots.telegram.message.TelegramBotMessageFactory;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.services.MarkOccurrenceProposalSubmissionService;

@Component
@RequiredArgsConstructor
public class ReadyToSubmitState implements ConversationState {

    private final MarkOccurrenceProposalSubmissionService submissionService;
    private final TelegramBotMessageFactory messageFactory;

    @Override
    public ProposalStatus getAssociatedStatus() {
        return ProposalStatus.READY_TO_SUBMIT;
    }

    @Override
    public BotApiMethod<?> handleSubmitCommand(ConversationContext context) {
        if (context.getProposalId() == null) {
            return messageFactory.createNothingToSubmitMessage(context.getChatId());
        }

        submissionService.submit(context.getProposalId());
        return messageFactory.createSubmissionSuccessMessage(context.getChatId());
    }
}
