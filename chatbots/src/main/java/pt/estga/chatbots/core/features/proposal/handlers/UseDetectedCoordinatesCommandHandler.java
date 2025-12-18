package pt.estga.chatbots.core.features.proposal.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.infrastructure.CommandHandler;
import pt.estga.chatbots.core.features.proposal.commands.UseDetectedCoordinatesCommand;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MonumentService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UseDetectedCoordinatesCommandHandler implements CommandHandler<UseDetectedCoordinatesCommand> {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final Cache<String, ConversationContext> conversationContexts;
    private final ObjectMapper objectMapper;
    private final MonumentService monumentService;

    @Override
    public BotResponse handle(UseDetectedCoordinatesCommand command) {
        ConversationContext context = conversationContexts.get(command.getInput().getUserId(), k -> new ConversationContext());
        MarkOccurrenceProposal proposal = context.getProposal();
        
        // The coordinates are already in the proposal, so we just need to trigger the monument lookup.
        // The addLocationToProposal method also triggers the monument lookup.
        MarkOccurrenceProposal updatedProposal = proposalFlowService.addLocationToProposal(proposal.getId(), proposal.getLatitude(), proposal.getLongitude());
        context.setProposal(updatedProposal);
        context.setCurrentStateName("WAITING_FOR_MONUMENT_CONFIRMATION");

        try {
            List<String> suggestedMonumentIds = objectMapper.readValue(updatedProposal.getSuggestedMonumentIds(), new TypeReference<>() {});
            if (suggestedMonumentIds.isEmpty()) {
                // No monuments found, proceed to new monument proposal
                context.setCurrentStateName("AWAITING_NEW_MONUMENT_NAME");
                return BotResponse.builder()
                        .uiComponent(Menu.builder().title("No nearby monuments found. Please enter the monument name.").build())
                        .build();
            }

            // For simplicity, we'll just use the first suggested monument.
            Optional<Monument> monumentOptional = monumentService.findById(Long.valueOf(suggestedMonumentIds.get(0)));
            if (monumentOptional.isPresent()) {
                Monument monument = monumentOptional.get();
                Menu monumentConfirmationMenu = Menu.builder()
                        .title("Was this photo taken at " + monument.getName() + "?")
                        .buttons(List.of(
                                List.of(
                                        Button.builder().text("✅ Yes").callbackData("confirm_monument:yes:" + monument.getId()).build(),
                                        Button.builder().text("❌ No").callbackData("confirm_monument:no").build()
                                )
                        ))
                        .build();
                return BotResponse.builder().uiComponent(monumentConfirmationMenu).build();
            } else {
                // Handle case where monument ID is invalid
                return BotResponse.builder()
                        .uiComponent(Menu.builder().title("Error processing monument suggestions.").build())
                        .build();
            }
        } catch (JsonProcessingException e) {
            // Handle exception
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing monument suggestions.").build())
                    .build();
        }
    }
}
