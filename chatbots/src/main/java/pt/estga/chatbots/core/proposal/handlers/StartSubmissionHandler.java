package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Button;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.services.UiTextService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StartSubmissionHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalService proposalService;
    private final UiTextService textService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getCallbackData() != null && input.getCallbackData().equals(ProposalCallbackData.START_SUBMISSION)) {

            Optional<MarkOccurrenceProposal> existingProposal = proposalService.findIncompleteByUserId(context.getDomainUserId());

            if (existingProposal.isPresent()) {
                context.setProposal(existingProposal.get());
                context.setCurrentState(ConversationState.AWAITING_PROPOSAL_ACTION);

                Menu confirmationMenu = Menu.builder()
                        .titleNode(textService.get(Messages.INCOMPLETE_SUBMISSION_TITLE))
                        .buttons(List.of(
                                List.of(Button.builder().textNode(textService.get(Messages.CONTINUE_SUBMISSION_BTN)).callbackData(ProposalCallbackData.CONTINUE_PROPOSAL).build()),
                                List.of(Button.builder().textNode(textService.get(Messages.START_NEW_SUBMISSION_BTN)).callbackData(ProposalCallbackData.DELETE_AND_START_NEW).build())
                        ))
                        .build();
                return Collections.singletonList(BotResponse.builder().uiComponent(confirmationMenu).build());
            }

            context.setCurrentState(ConversationState.WAITING_FOR_PHOTO);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(textService.get(Messages.SEND_PHOTO_PROMPT)).build())
                    .build());
        }
        return null;
    }

    @Override
    public ConversationState canHandle() {
        return null;
    }
}
