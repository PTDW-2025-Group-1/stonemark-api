package pt.estga.chatbots.core.verification.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.context.HandlerOutcome;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.user.entities.User;
import pt.estga.user.enums.ContactType;
import pt.estga.user.services.UserContactService;
import pt.estga.user.services.UserIdentityService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmitContactHandler implements ConversationStateHandler {

    private final UserContactService userContactService;
    private final UserIdentityService userIdentityService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        if (input.getType() != BotInput.InputType.CONTACT || input.getText() == null) {
            return HandlerOutcome.FAILURE;
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
            
            // Store phone number in context in case it's needed for code verification
            context.setVerificationPhoneNumber(phoneNumber);

            return HandlerOutcome.SUCCESS;
        } else {
            log.warn("No user found for phone number: {}", phoneNumber);
            return HandlerOutcome.FAILURE;
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_CONTACT;
    }
}
