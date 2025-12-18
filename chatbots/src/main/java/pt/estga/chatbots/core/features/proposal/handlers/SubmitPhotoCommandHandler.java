package pt.estga.chatbots.core.features.proposal.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.infrastructure.CommandHandler;
import pt.estga.chatbots.core.features.proposal.commands.SubmitPhotoCommand;
import pt.estga.chatbots.core.context.ConversationContext;
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
public class SubmitPhotoCommandHandler implements CommandHandler<SubmitPhotoCommand> {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final MarkOccurrenceProposalSubmissionService submissionService;
    private final Cache<String, ConversationContext> conversationContexts;

    @Override
    public BotResponse handle(SubmitPhotoCommand command) {
        ConversationContext context = conversationContexts.get(command.getInput().getUserId(), k -> new ConversationContext());

        try {
            // 1. Initiate the proposal
            Long domainUserId = context.getDomainUserId();
            log.info("Initiating proposal for user ID: {} (domain ID: {}) with file: {}", command.getInput().getUserId(), domainUserId, command.getInput().getFileName());
            MarkOccurrenceProposal proposal = proposalFlowService.initiate(domainUserId, command.getInput().getFileData(), command.getInput().getFileName());
            context.setProposal(proposal);
            log.info("Proposal with ID {} created.", proposal.getId());

            // 2. Submit the proposal for detection
            log.info("Submitting proposal with ID: {}", proposal.getId());
            submissionService.submit(proposal.getId());
            log.info("Proposal with ID {} submitted successfully.", proposal.getId());
            
            context.setCurrentStateName("WAITING_FOR_MARK_CONFIRMATION");
            log.info("Context state set to WAITING_FOR_MARK_CONFIRMATION.");

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
            log.error("Error processing photo for user: {}", command.getInput().getUserId(), e);
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing photo.").build())
                    .build();
        }
    }
}
