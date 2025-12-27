package pt.estga.chatbot.features.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.VerificationState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.chatbot.services.AuthService;
import pt.estga.chatbot.services.AuthServiceFactory;
import pt.estga.chatbot.features.verification.VerificationCallbackData;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuthenticationGuard {

    private final AuthServiceFactory authServiceFactory;

    // Whitelist commands that an unauthenticated user can use
    private static final Set<String> ALLOWED_UNAUTHENTICATED_COMMANDS = Set.of(
            "/start",
            "/options",
            "/help"
    );

    public boolean isActionAllowed(BotInput input, ConversationState currentState) {
        // If the user is authenticated, always allow the action.
        if (isAuthenticated(input)) {
            return true;
        }

        // If the user is not authenticated, only allow specific actions.
        if (input.getType() == BotInput.InputType.TEXT && input.getText() != null) {
            // Allow whitelisted commands (e.g., /start)
            if (ALLOWED_UNAUTHENTICATED_COMMANDS.stream().anyMatch(cmd -> input.getText().startsWith(cmd))) {
                return true;
            }
            // Allow user to submit a verification code if they are in that part of the conversation
            if (currentState == VerificationState.AWAITING_VERIFICATION_CODE) {
                return true;
            }
            return currentState == VerificationState.AWAITING_CONTACT;
        }

        // Allow starting the verification process and choosing a method
        if (input.getType() == BotInput.InputType.CALLBACK) {
            String callbackData = input.getCallbackData();
            return VerificationCallbackData.START_VERIFICATION.equals(callbackData) ||
                   VerificationCallbackData.CHOOSE_VERIFY_WITH_CODE.equals(callbackData) ||
                   VerificationCallbackData.CHOOSE_VERIFY_WITH_PHONE.equals(callbackData);
        }

        // Allow sending a contact
        if (input.getType() == BotInput.InputType.CONTACT) {
            return currentState == VerificationState.AWAITING_CONTACT;
        }

        return false;
    }

    private boolean isAuthenticated(BotInput input) {
        if (input == null || input.getPlatform() == null || input.getUserId() == null) {
            return false;
        }
        AuthService authService = authServiceFactory.getAuthService(input.getPlatform());
        return authService.isAuthenticated(input.getUserId());
    }
}
