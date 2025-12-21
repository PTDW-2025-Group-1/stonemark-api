package pt.estga.chatbots.core.verification.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.handlers.OptionsMessageHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.user.entities.User;
import pt.estga.user.enums.ContactType;
import pt.estga.user.services.UserContactService;
import pt.estga.user.services.UserIdentityService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmitContactHandler implements ConversationStateHandler {

    private final UserContactService userContactService;
    private final UserIdentityService userIdentityService;
    private final OptionsMessageHandler optionsMessageHandler;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getType() != BotInput.InputType.CONTACT || input.getText() == null) {
            return null;
        }

        String phoneNumber = input.getText();
        Optional<User> userOptional = userContactService.findUserByPhoneNumber(phoneNumber);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Create a verified contact for the user
            userContactService.createVerifiedContact(user, ContactType.TELEPHONE, phoneNumber);
            // Associate the Telegram ID with the user
            userIdentityService.createOrUpdateTelegramIdentity(user, input.getUserId());
            
            log.info("Successfully verified user {} and associated their Telegram ID.", user.getUsername());

            List<BotResponse> responses = new ArrayList<>();
            responses.add(BotResponse.builder().text("Thank you, " + user.getFirstName() + "! Your account is now verified.").build());
            responses.addAll(optionsMessageHandler.handle(context, input));
            return responses;
            
        } else {
            log.warn("No user found for phone number: {}", phoneNumber);
            return List.of(BotResponse.builder().text("Sorry, we couldn't find an account associated with that phone number. Please try again or contact support.").build());
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_CONTACT;
    }
}
