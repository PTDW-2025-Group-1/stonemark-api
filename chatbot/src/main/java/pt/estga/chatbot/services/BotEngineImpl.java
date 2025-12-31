package pt.estga.chatbot.services;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
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
import pt.estga.shared.enums.PrincipalType;
import pt.estga.shared.models.AppPrincipal;
import pt.estga.shared.utils.SecurityUtils;
import pt.estga.user.enums.Provider;
import pt.estga.user.services.UserIdentityService;

import java.util.List;

@Service
public class BotEngineImpl implements BotEngine {

    private final ConversationDispatcher conversationDispatcher;
    private final Cache<String, ChatbotContext> conversationContexts;
    private final AuthenticationGuard authenticationGuard;
    private final AuthResponseProvider authenticationGuardHandler;
    private final AuthService authService;
    private final UserIdentityService userIdentityService;

    public BotEngineImpl(
            ConversationDispatcher conversationDispatcher,
            Cache<String, ChatbotContext> conversationContexts,
            AuthenticationGuard authenticationGuard,
            AuthResponseProvider authenticationGuardHandler,
            @Qualifier("telegramAuthService") AuthService authService,
            UserIdentityService userIdentityService
    ) {
        this.conversationDispatcher = conversationDispatcher;
        this.conversationContexts = conversationContexts;
        this.authenticationGuard = authenticationGuard;
        this.authenticationGuardHandler = authenticationGuardHandler;
        this.authService = authService;
        this.userIdentityService = userIdentityService;
    }

    @Override
    public List<BotResponse> handleInput(BotInput input) {
        String userId = input.getUserId();
        var currentUserId = SecurityUtils.getCurrentUserId();

        if (userId == null) {
            userId = currentUserId
                    .map(String::valueOf)
                    .orElse(null);
        }

        ChatbotContext context = getOrCreateContext(userId);

        currentUserId.ifPresent(id -> {
            if (context.getDomainUserId() == null) {
                context.setDomainUserId(id);
            }
        });

        authenticateUserIfPossible(context, userId);

        if (isGlobalCommand(input)) {
            resetContext(context);
        }

        if (context.getCurrentState() == null) {
            context.setCurrentState(CoreState.START);
        }

        if (!authenticationGuard.isActionAllowed(input, context.getCurrentState())) {
            return authenticationGuardHandler.requireVerification(context);
        }

        return conversationDispatcher.dispatch(context, input);
    }

    private ChatbotContext getOrCreateContext(String userId) {
        return conversationContexts.get(userId, k -> new ChatbotContext());
    }

    private void authenticateUserIfPossible(ChatbotContext context, String userId) {
        if (context.getDomainUserId() == null && authService.isAuthenticated(userId)) {
            userIdentityService.findByProviderAndValue(Provider.TELEGRAM, userId)
                    .ifPresent(userIdentity -> {
                        var user = userIdentity.getUser();
                        context.setDomainUserId(user.getId());
                        AppPrincipal principal = AppPrincipal.builder()
                                .id(user.getId())
                                .type(PrincipalType.USER)
                                .identifier(user.getUsername())
                                .password(user.getPassword())
                                .authorities(SecurityUtils.mapUserRolesToAuthorities(user.getRole()))
                                .enabled(user.isEnabled())
                                .accountNonLocked(!user.isAccountLocked())
                                .build();
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }
    }

    private boolean isGlobalCommand(BotInput input) {
        String text = input.getText();
        String callbackData = input.getCallbackData();

        boolean isStartCommand = text != null && text.startsWith("/start");
        boolean isHelpCommand = text != null && text.startsWith("/help");
        boolean isOptionsCommand = text != null && text.startsWith("/options");
        boolean isBackToMenu = callbackData != null && callbackData.equals(SharedCallbackData.BACK_TO_MAIN_MENU);

        return isStartCommand || isHelpCommand || isOptionsCommand || isBackToMenu;
    }

    private void resetContext(ChatbotContext context) {
        context.setCurrentState(CoreState.START);
        context.getProposalContext().clear();
    }
}
