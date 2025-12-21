package pt.estga.chatbots.core.features.proposal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonumentProcessorService {

    public BotResponse processMonumentStep(ConversationContext context, MarkOccurrenceProposal updatedProposal) {
        log.info("Processing monument step for proposal ID: {}", updatedProposal.getId());
        context.setProposal(updatedProposal);
        context.setCurrentState(ConversationState.LOOP_OPTIONS);

        return BotResponse.builder()
                .uiComponent(Menu.builder()
                        .title("What would you like to do next?")
                        .buttons(List.of(
                                List.of(Button.builder().text("Reselect Location").callbackData(CallbackData.LOOP_REDO_LOCATION).build()),
                                List.of(Button.builder().text("Redo Image Upload").callbackData(CallbackData.LOOP_REDO_IMAGE_UPLOAD).build()),
                                List.of(Button.builder().text("Continue to Notes").callbackData(CallbackData.LOOP_CONTINUE).build())
                        ))
                        .build())
                .build();
    }
}
