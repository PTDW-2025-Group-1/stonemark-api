package pt.estga.chatbots.core.features.proposal.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.infrastructure.CommandHandler;
import pt.estga.chatbots.core.features.proposal.commands.ConfirmMonumentCommand;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
public class ConfirmMonumentCommandHandler implements CommandHandler<ConfirmMonumentCommand> {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final Cache<String, ConversationContext> conversationContexts;

    @Override
    public BotResponse handle(ConfirmMonumentCommand command) {
        ConversationContext context = conversationContexts.get(command.getInput().getUserId(), k -> new ConversationContext());
        var proposal = context.getProposal();

        if (command.isConfirmed()) {
            proposalFlowService.selectMonument(proposal.getId(), command.getMonumentId());
            context.setCurrentStateName("READY_TO_SUBMIT");
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Thank you! Your proposal has been submitted and will be reviewed.").build())
                    .build();
        } else {
            context.setCurrentStateName("AWAITING_NEW_MONUMENT_NAME");
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Understood. Please enter the monument name.").build())
                    .build();
        }
    }
}
