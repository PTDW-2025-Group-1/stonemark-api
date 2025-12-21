package pt.estga.chatbots.core.verification.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.handlers.OptionsMessageHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.user.entities.User;
import pt.estga.verification.services.ChatbotVerificationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubmitVerificationCodeHandler implements ConversationStateHandler {

    private final ChatbotVerificationService verificationService;
    private final OptionsMessageHandler optionsMessageHandler;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        String code = input.getText();
        Optional<User> userOptional = verificationService.verifyTelegramCode(code, input.getUserId());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            context.setDomainUserId(user.getId());
            context.setCurrentState(ConversationState.START);

            List<BotResponse> responses = new ArrayList<>();
            responses.add(BotResponse.builder().text("Thank you, " + user.getFirstName() + "! Your account has been successfully verified.").build());
            responses.addAll(optionsMessageHandler.handle(context, input));

            return responses;
        } else {
            return Collections.singletonList(BotResponse.builder()
                    .text("That code is invalid or has expired. Please try again.")
                    .build());
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_VERIFICATION_CODE;
    }
}
