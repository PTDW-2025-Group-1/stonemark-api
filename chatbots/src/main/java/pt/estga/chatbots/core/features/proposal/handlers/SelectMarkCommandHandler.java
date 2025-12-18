package pt.estga.chatbots.core.features.proposal.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.infrastructure.CommandHandler;
import pt.estga.chatbots.core.features.proposal.commands.SelectMarkCommand;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
public class SelectMarkCommandHandler implements CommandHandler<SelectMarkCommand> {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final Cache<String, ConversationContext> conversationContexts;

    @Override
    public BotResponse handle(SelectMarkCommand command) {
        ConversationContext context = conversationContexts.get(command.getInput().getUserId(), k -> new ConversationContext());
        var proposal = context.getProposal();
        proposalFlowService.selectMark(proposal.getId(), command.getMarkId());
        context.setCurrentStateName("READY_TO_SUBMIT");

        return BotResponse.builder()
                .uiComponent(Menu.builder().title("Your proposal is ready to submit.").build())
                .build();
    }
}
