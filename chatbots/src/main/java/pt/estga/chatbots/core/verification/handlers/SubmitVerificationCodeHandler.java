package pt.estga.chatbots.core.verification.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.context.HandlerOutcome;
import pt.estga.chatbots.core.shared.context.VerificationState;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.user.entities.User;
import pt.estga.user.enums.ContactType;
import pt.estga.user.services.UserContactService;
import pt.estga.user.services.UserIdentityService;
import pt.estga.verification.services.ChatbotVerificationService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmitVerificationCodeHandler implements ConversationStateHandler {

    private final ChatbotVerificationService verificationService;
    private final UserContactService userContactService;
    private final UserIdentityService userIdentityService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        String code = input.getText();
        String phoneNumber = context.getVerificationPhoneNumber();

        if (phoneNumber == null) {
            log.error("Verification phone number is missing from conversation context for user {}", input.getUserId());
            return HandlerOutcome.FAILURE;
        }

        Optional<User> userOptional = verificationService.verifyTelegramCode(code, input.getUserId());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            context.setDomainUserId(user.getId());
            context.setVerificationPhoneNumber(null); // Clean up context

            // Create verified contact and identity
            userContactService.createVerifiedContact(user, ContactType.TELEPHONE, phoneNumber);
            userIdentityService.createOrUpdateTelegramIdentity(user, input.getUserId());

            log.info("Successfully verified user {} with phone number and created Telegram identity.", user.getUsername());

            return HandlerOutcome.SUCCESS;
        } else {
            return HandlerOutcome.FAILURE;
        }
    }

    @Override
    public ConversationState canHandle() {
        return VerificationState.AWAITING_VERIFICATION_CODE;
    }
}
