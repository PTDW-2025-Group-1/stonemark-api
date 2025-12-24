package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.handlers.StartHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Button;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.services.UiTextService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalService;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubmissionLoopHandler implements ConversationStateHandler {

    private final StartHandler startHandler;
    private final MarkOccurrenceProposalService proposalService;
    private final UiTextService textService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();
        MarkOccurrenceProposal proposal = context.getProposal();

        if (callbackData == null) {
            return showOptions(proposal);
        }

        switch (callbackData) {
            case ProposalCallbackData.SUBMISSION_LOOP_START_OVER:
                return showConfirmation(proposal);

            case ProposalCallbackData.SUBMISSION_LOOP_START_OVER_CONFIRMED:
                proposalService.delete(proposal);
                context.setProposal(null);
                return startHandler.handle(context, BotInput.builder().text("/start").build());

            case ProposalCallbackData.SUBMISSION_LOOP_CONTINUE:
                context.setCurrentState(ConversationState.AWAITING_NOTES);
                Menu notesMenu = Menu.builder()
                        .titleNode(textService.get(Messages.ADD_NOTES_PROMPT))
                        .buttons(List.of(
                                List.of(Button.builder().textNode(textService.get(Messages.SKIP_BTN)).callbackData(ProposalCallbackData.SKIP_NOTES).build())
                        ))
                        .build();
                return Collections.singletonList(BotResponse.builder().uiComponent(notesMenu).build());

            default:
                return showOptions(proposal);
        }
    }

    private List<BotResponse> showOptions(MarkOccurrenceProposal proposal) {
        Menu menu = Menu.builder()
                .titleNode(textService.get(Messages.SUBMISSION_LOOP_TITLE))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(Messages.DISCARD_SUBMISSION_BTN)).callbackData(ProposalCallbackData.SUBMISSION_LOOP_START_OVER).build()),
                        List.of(Button.builder().textNode(textService.get(Messages.CONTINUE_TO_SUBMIT_BTN)).callbackData(ProposalCallbackData.SUBMISSION_LOOP_CONTINUE).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> showConfirmation(MarkOccurrenceProposal proposal) {
        Menu menu = Menu.builder()
                .titleNode(textService.get(Messages.DISCARD_CONFIRMATION_TITLE))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(Messages.YES_DISCARD_BTN)).callbackData(ProposalCallbackData.SUBMISSION_LOOP_START_OVER_CONFIRMED).build()),
                        List.of(Button.builder().textNode(textService.get(Messages.NO_GO_BACK_BTN)).callbackData(ProposalCallbackData.SUBMISSION_LOOP_OPTIONS).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.SUBMISSION_LOOP_OPTIONS;
    }
}
