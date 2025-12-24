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
import pt.estga.content.entities.Mark;
import pt.estga.content.services.MarkService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class SelectMonumentHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;
    private final MarkService markService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();
        
        if (callbackData == null || !callbackData.startsWith(ProposalCallbackData.SELECT_MONUMENT_PREFIX)) {
             return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title(Messages.SELECT_MONUMENT_PROMPT).build())
                    .build());
        }

        var proposal = context.getProposal();
        Long monumentId = Long.valueOf(callbackData.substring(ProposalCallbackData.SELECT_MONUMENT_PREFIX.length()));
        MarkOccurrenceProposal updatedProposal = proposalFlowService.selectMonument(proposal.getId(), monumentId);
        context.setProposal(updatedProposal);
        context.setCurrentState(ConversationState.LOOP_OPTIONS);

        List<String> suggestedMarkIds = proposalFlowService.getSuggestedMarkIds(updatedProposal.getId());
        List<List<Button>> markButtons = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        for (String markId : suggestedMarkIds) {
            Optional<Mark> markOptional = markService.findById(Long.valueOf(markId));
            markOptional.ifPresent(mark -> {
                List<Button> row = new ArrayList<>();
                row.add(Button.builder().text(counter.getAndIncrement() + ". Mark #" + mark.getId())
                        .callbackData(ProposalCallbackData.SELECT_MARK_PREFIX + mark.getId()).build());
                markButtons.add(row);
            });
        }

        List<Button> proposeNewRow = new ArrayList<>();
        proposeNewRow.add(Button.builder().text(Messages.PROPOSE_NEW_MARK_BTN).callbackData(ProposalCallbackData.PROPOSE_NEW_MARK).build());
        markButtons.add(proposeNewRow);

        Menu markSelectionMenu = Menu.builder()
                .title(Messages.FOUND_MARKS_TITLE)
                .buttons(markButtons)
                .build();

        return Collections.singletonList(BotResponse.builder()
                .uiComponent(markSelectionMenu)
                .build());
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_MONUMENT_CONFIRMATION;
    }
}
