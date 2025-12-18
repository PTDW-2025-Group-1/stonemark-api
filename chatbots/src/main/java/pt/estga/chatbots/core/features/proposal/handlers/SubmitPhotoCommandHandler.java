package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;
import pt.estga.proposals.services.MarkOccurrenceProposalSubmissionService;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmitPhotoCommandHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final MarkOccurrenceProposalSubmissionService submissionService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        try {
            // 1. Initiate the proposal
            Long domainUserId = context.getDomainUserId();
            log.info("Initiating proposal for user ID: {} (domain ID: {}) with file: {}", input.getUserId(), domainUserId, input.getFileName());
            MarkOccurrenceProposal proposal = proposalFlowService.initiate(domainUserId, input.getFileData(), input.getFileName());
            context.setProposal(proposal);
            log.info("Proposal with ID {} created.", proposal.getId());

            // 2. Submit the proposal for detection
            log.info("Submitting proposal with ID: {}", proposal.getId());
            submissionService.submit(proposal.getId());
            log.info("Proposal with ID {} submitted successfully.", proposal.getId());

            context.setCurrentState(ConversationState.WAITING_FOR_MARK_CONFIRMATION);
            log.info("Context state set to {}.", ConversationState.WAITING_FOR_MARK_CONFIRMATION);

            // For now, we'll just ask a dummy confirmation.
            Menu confirmationMenu = Menu.builder()
                    .title("Does this pattern match the one in your photo?")
                    .buttons(List.of(
                            List.of(
                                    Button.builder().text("✅ Yes, it matches").callbackData("confirm_mark_match:yes").build(),
                                    Button.builder().text("❌ No, it doesn’t match").callbackData("confirm_mark_match:no").build()
                            )
                    ))
                    .build();

            return BotResponse.builder()
                    .uiComponent(confirmationMenu)
                    .build();

        } catch (IOException e) {
            log.error("Error processing photo for user: {}", input.getUserId(), e);
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing photo.").build())
                    .build();
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_PHOTO;
    }
}
