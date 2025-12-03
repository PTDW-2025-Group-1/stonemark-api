package pt.estga.bots.telegram.callback;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import pt.estga.bots.telegram.context.ConversationContext;
import pt.estga.bots.telegram.message.TelegramBotMessageFactory;
import pt.estga.bots.telegram.state.factory.StateFactory;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
public class CallbackQueryHandlerImpl implements CallbackQueryHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final TelegramBotMessageFactory messageFactory;
    private final StateFactory stateFactory;

    @Override
    public BotApiMethod<?> handle(ConversationContext context, String callbackQueryId, String callbackData) {
        if (context == null || context.getProposalId() == null) {
            return new AnswerCallbackQuery(callbackQueryId);
        }

        MarkOccurrenceProposal proposal = null;

        if (callbackData.startsWith("SELECT_MARK:")) {
            try {
                Long markId = Long.parseLong(callbackData.substring("SELECT_MARK:".length()));
                proposal = proposalFlowService.selectMark(context.getProposalId(), markId);
            } catch (NumberFormatException e) {
                AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQueryId);
                answerCallbackQuery.setText("Invalid mark ID format.");
                return answerCallbackQuery;
            }
        } else if (callbackData.equals("PROPOSE_NEW_MARK")) {
            proposal = proposalFlowService.requestNewMark(context.getProposalId());
            context.setState(stateFactory.createState(proposal.getStatus()));
            return messageFactory.createAwaitingMarkDetailsMessage(context.getChatId());
        } else if (callbackData.equals("CONFIRM_MONUMENT_LOCATION:YES")) {
            proposal = proposalFlowService.confirmMonumentLocation(context.getProposalId(), true);
        } else if (callbackData.equals("CONFIRM_MONUMENT_LOCATION:NO")) {
            proposal = proposalFlowService.confirmMonumentLocation(context.getProposalId(), false);
        } else if (callbackData.startsWith("SELECT_MONUMENT:")) {
            try {
                Long monumentId = Long.parseLong(callbackData.substring("SELECT_MONUMENT:".length()));
                proposal = proposalFlowService.selectMonument(context.getProposalId(), monumentId);
            } catch (NumberFormatException e) {
                AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQueryId);
                answerCallbackQuery.setText("Invalid monument ID format.");
                return answerCallbackQuery;
            }
        } else if (callbackData.equals("PROPOSE_NEW_MONUMENT")) {
            proposal = proposalFlowService.requestNewMonument(context.getProposalId());
        }

        if (proposal != null) {
            context.setState(stateFactory.createState(proposal.getStatus()));
            return messageFactory.createMessageForProposalStatus(context.getChatId(), proposal);
        }

        return new AnswerCallbackQuery(callbackQueryId);
    }
}
