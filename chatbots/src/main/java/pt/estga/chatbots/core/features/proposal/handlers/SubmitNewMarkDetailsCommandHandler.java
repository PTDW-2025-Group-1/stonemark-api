package pt.estga.chatbots.core.features.proposal.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.infrastructure.CommandHandler;
import pt.estga.chatbots.core.features.proposal.commands.SubmitNewMarkDetailsCommand;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
public class SubmitNewMarkDetailsCommandHandler implements CommandHandler<SubmitNewMarkDetailsCommand> {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final Cache<String, ConversationContext> conversationContexts;

    @Override
    public BotResponse handle(SubmitNewMarkDetailsCommand command) {
        ConversationContext context = conversationContexts.get(command.getInput().getUserId(), k -> new ConversationContext());
        var proposal = context.getProposal();
        String[] details = command.getInput().getText().split("\n");
        String title = details[0];
        String description = details.length > 1 ? details[1] : "";
        proposalFlowService.proposeMark(proposal.getId(), title, description);
        context.setCurrentStateName("READY_TO_SUBMIT");

        return BotResponse.builder()
                .uiComponent(Menu.builder().title("Your proposal is ready to submit.").build())
                .build();
    }
}
