package pt.estga.chatbots.core.features.proposal.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.infrastructure.CommandHandler;
import pt.estga.chatbots.core.features.proposal.commands.SubmitNewMonumentNameCommand;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.content.entities.Mark;
import pt.estga.content.services.MarkService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubmitNewMonumentNameCommandHandler implements CommandHandler<SubmitNewMonumentNameCommand> {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final Cache<String, ConversationContext> conversationContexts;
    private final ObjectMapper objectMapper;
    private final MarkService markService;

    @Override
    public BotResponse handle(SubmitNewMonumentNameCommand command) {
        ConversationContext context = conversationContexts.get(command.getInput().getUserId(), k -> new ConversationContext());
        var proposal = context.getProposal();
        MarkOccurrenceProposal updatedProposal = proposalFlowService.proposeMonument(
                proposal.getId(),
                command.getInput().getText(),
                proposal.getLatitude(),
                proposal.getLongitude()
        );
        context.setProposal(updatedProposal);
        context.setCurrentStateName("AWAITING_MARK_SELECTION");

        try {
            List<String> suggestedMarkIds = objectMapper.readValue(updatedProposal.getSuggestedMarkIds(), new TypeReference<>() {});
            List<List<Button>> markButtons = new ArrayList<>();
            for (String markId : suggestedMarkIds) {
                Optional<Mark> markOptional = markService.findById(Long.valueOf(markId));
                markOptional.ifPresent(mark -> {
                    List<Button> row = new ArrayList<>();
                    row.add(Button.builder().text(mark.getTitle()).callbackData("select_mark:" + mark.getId()).build());
                    markButtons.add(row);
                });
            }

            List<Button> proposeNewRow = new ArrayList<>();
            proposeNewRow.add(Button.builder().text("Propose New Mark").callbackData("propose_new_mark").build());
            markButtons.add(proposeNewRow);

            Menu markSelectionMenu = Menu.builder()
                    .title("I found some marks that might match. Please select one or propose a new one:")
                    .buttons(markButtons)
                    .build();

            return BotResponse.builder()
                    .uiComponent(markSelectionMenu)
                    .build();
        } catch (JsonProcessingException e) {
            // Handle exception
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing mark suggestions.").build())
                    .build();
        }
    }
}
