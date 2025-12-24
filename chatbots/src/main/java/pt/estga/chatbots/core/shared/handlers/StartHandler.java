package pt.estga.chatbots.core.shared.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserIdentity;
import pt.estga.user.enums.Provider;
import pt.estga.user.services.UserIdentityService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class StartHandler implements ConversationStateHandler {

    private final OptionsMessageHandler optionsMessageHandler;
    private final UserIdentityService userIdentityService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getText() != null && input.getText().startsWith("/start")) {
            context.setCurrentState(ConversationState.START);

            String welcomeMessage = "Welcome!";
            try {
                Optional<User> userOptional = userIdentityService
                        .findByProviderAndValue(Provider.valueOf(input.getPlatform()), input.getUserId())
                        .map(UserIdentity::getUser);

                if (userOptional.isPresent()) {
                    welcomeMessage = "Welcome back, " + userOptional.get().getFirstName() + "!";
                } else {
                    log.warn("User not found for platform {} and userId {}", input.getPlatform(), input.getUserId());
                }
            } catch (Exception e) {
                log.error("Error retrieving user for welcome message", e);
            }

            List<BotResponse> responses = new ArrayList<>();
            responses.add(BotResponse.builder().text(welcomeMessage).build());
            responses.addAll(optionsMessageHandler.handle(context, input));

            return responses;
        }
        return null;
    }

    @Override
    public ConversationState canHandle() {
        return null; // Global handler
    }
}
