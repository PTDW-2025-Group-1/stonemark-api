package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.handlers.StartHandler;
import pt.estga.chatbots.core.proposal.service.MarkProcessorService;
import pt.estga.chatbots.core.proposal.service.MonumentSuggestionProcessorService;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Button;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalService;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubmissionLoopHandler implements ConversationStateHandler {

    private final MarkProcessorService markProcessorService;
    private final MonumentSuggestionProcessorService monumentSuggestionProcessorService;
    private final StartHandler startHandler;
    private final MarkOccurrenceProposalService proposalService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();
        MarkOccurrenceProposal proposal = context.getProposal();

        if (callbackData == null) {
            return showOptions(proposal);
        }

        switch (callbackData) {
            case ProposalCallbackData.SUBMISSION_LOOP_CHANGE_MARK:
                return markProcessorService.processMarkSuggestions(context, proposal);

            case ProposalCallbackData.SUBMISSION_LOOP_CHANGE_MONUMENT:
                return monumentSuggestionProcessorService.processMonumentSuggestions(context, proposal);

            case ProposalCallbackData.SUBMISSION_LOOP_START_OVER:
                return showConfirmation(proposal);

            case ProposalCallbackData.SUBMISSION_LOOP_START_OVER_CONFIRMED:
                proposalService.delete(proposal);
                context.setProposal(null);
                return startHandler.handle(context, BotInput.builder().text("/start").build());

            case ProposalCallbackData.SUBMISSION_LOOP_CONTINUE:
                context.setCurrentState(ConversationState.AWAITING_NOTES);
                Menu notesMenu = Menu.builder()
                        .title("Please add any notes for this proposal, or you can skip this step.")
                        .buttons(List.of(
                                List.of(Button.builder().text("Skip").callbackData(ProposalCallbackData.SKIP_NOTES).build())
                        ))
                        .build();
                return Collections.singletonList(BotResponse.builder().uiComponent(notesMenu).build());

            default:
                return showOptions(proposal);
        }
    }

    private List<BotResponse> showOptions(MarkOccurrenceProposal proposal) {
        Menu menu = Menu.builder()
                .title("Review your submission. What would you like to do next?")
                .buttons(List.of(
                        List.of(Button.builder().text("Change Mark").callbackData(ProposalCallbackData.SUBMISSION_LOOP_CHANGE_MARK).build()),
                        List.of(Button.builder().text("Change Monument").callbackData(ProposalCallbackData.SUBMISSION_LOOP_CHANGE_MONUMENT).build()),
                        List.of(Button.builder().text("Discard Submission").callbackData(ProposalCallbackData.SUBMISSION_LOOP_START_OVER).build()),
                        List.of(Button.builder().text("Continue to Submit").callbackData(ProposalCallbackData.SUBMISSION_LOOP_CONTINUE).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> showConfirmation(MarkOccurrenceProposal proposal) {
        Menu menu = Menu.builder()
                .title("Are you sure you want to discard this submission?")
                .buttons(List.of(
                        List.of(Button.builder().text("Yes, Discard").callbackData(ProposalCallbackData.SUBMISSION_LOOP_START_OVER_CONFIRMED).build()),
                        List.of(Button.builder().text("No, Go Back").callbackData(ProposalCallbackData.SUBMISSION_LOOP_OPTIONS).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.SUBMISSION_LOOP_OPTIONS;
    }
}
