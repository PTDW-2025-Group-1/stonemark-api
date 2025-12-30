package pt.estga.chatbot.services;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pt.estga.chatbot.constants.SharedCallbackData;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.CoreState;
import pt.estga.chatbot.features.auth.AuthResponseProvider;
import pt.estga.chatbot.features.auth.AuthenticationGuard;
import pt.estga.chatbot.models.BotInput;
import pt.estga.chatbot.models.BotResponse;
import pt.estga.shared.models.ServiceAccountPrincipal;
import pt.estga.user.enums.Provider;
import pt.estga.user.services.UserIdentityService;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BotConversationServiceImpl implements BotConversationService {

    private final ConversationDispatcher conversationDispatcher;
    private final Cache<String, ChatbotContext> conversationContexts;
    private final AuthenticationGuard authenticationGuard;
    private final AuthResponseProvider authenticationGuardHandler;
    private final AuthService authService;
    private final UserIdentityService userIdentityService;

    @Override
    public List<BotResponse> handleInput(BotInput input) {
        ChatbotContext context = conversationContexts.get(input.getUserId(), k -> new ChatbotContext());

        if (context.getDomainUserId() == null && authService.isAuthenticated(input.getUserId())) {
            userIdentityService.findByProviderAndValue(Provider.TELEGRAM, input.getUserId())
                    .ifPresent(userIdentity -> {
                        context.setDomainUserId(userIdentity.getUser().getId());
                        ServiceAccountPrincipal principal = ServiceAccountPrincipal.builder()
                                .id(userIdentity.getUser().getId())
                                .build();
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }

        // Handle global commands that reset the conversation
        boolean isStartCommand = input.getText() != null && input.getText().startsWith("/start");
        boolean isHelpCommand = input.getText() != null && input.getText().startsWith("/help");
        boolean isOptionsCommand = input.getText() != null && input.getText().startsWith("/options");
        boolean isBackToMenu = input.getCallbackData() != null && input.getCallbackData().equals(SharedCallbackData.BACK_TO_MAIN_MENU);

        if (isStartCommand || isHelpCommand || isOptionsCommand || isBackToMenu) {
            // Reset context and dispatch to the state machine.
            context.setCurrentState(CoreState.START);
            context.getProposalContext().clear();
        }

        if (context.getCurrentState() == null) {
            context.setCurrentState(CoreState.START);
        }

        // Perform authentication check for stateful conversation.
        if (!authenticationGuard.isActionAllowed(input, context.getCurrentState())) {
            return authenticationGuardHandler.requireVerification(context);
        }

        // Dispatch to the state machine for all other interactions.
        return conversationDispatcher.dispatch(context, input);
    }
}
