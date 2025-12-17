package pt.estga.chatbots.telegram.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Contact;
import pt.estga.chatbots.telegram.callback.CallbackQueryHandler;
import pt.estga.chatbots.telegram.config.TelegramBotProperties;
import pt.estga.chatbots.telegram.context.ConversationContext;
import pt.estga.chatbots.telegram.message.TelegramBotMessageFactory;
import pt.estga.chatbots.telegram.state.factory.StateFactory;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.shared.models.Location;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserIdentity;
import pt.estga.user.enums.Provider;
import pt.estga.user.repositories.UserIdentityRepository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotCommandServiceImpl implements TelegramBotCommandService {

    private final TelegramBotMessageFactory messageFactory;
    private final StateFactory stateFactory;
    private final CallbackQueryHandler callbackQueryHandler;
    private final UserIdentityRepository userIdentityRepository;
    private final TelegramBotProperties telegramBotProperties;

    private final Cache<Long, ConversationContext> conversationContexts = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES) // Evict entries after 30 minutes of inactivity
            .maximumSize(10_000) // Maximum number of entries in the cache
            .build();

    @Override
    public BotApiMethod<?> handleStartCommand(long chatId) {
        ConversationContext context = new ConversationContext(chatId);
        conversationContexts.put(chatId, context);
        return messageFactory.createStartMessage(chatId);
    }

    @Override
    public BotApiMethod<?> handleHelpCommand(long chatId) {
        return messageFactory.createHelpMessage(chatId);
    }

    @Override
    public BotApiMethod<?> handleCancelCommand(long chatId) {
        conversationContexts.invalidate(chatId);
        return messageFactory.createCancelMessage(chatId);
    }

    @Override
    public BotApiMethod<?> handlePhotoSubmission(long chatId, byte[] photoData, String fileName) {
        ConversationContext context = conversationContexts.getIfPresent(chatId);
        if (context != null) {
            return context.getState().handlePhotoSubmission(context, photoData, fileName);
        }
        return messageFactory.createInvalidInputForStateMessage(chatId);
    }

    @Override
    public BotApiMethod<?> handleLocationSubmission(long chatId, Location location) {
        ConversationContext context = conversationContexts.getIfPresent(chatId);
        if (context != null) {
            return context.getState().handleLocationSubmission(context, location);
        }
        return messageFactory.createInvalidInputForStateMessage(chatId);
    }

    @Override
    public BotApiMethod<?> handleTextMessage(long chatId, String messageText) {
        ConversationContext context = conversationContexts.getIfPresent(chatId);
        if (context != null) {
            return context.getState().handleTextMessage(context, messageText);
        }
        return messageFactory.createUnknownCommandHelpMessage(chatId);
    }

    @Override
    public BotApiMethod<?> handleCallbackQuery(long chatId, String callbackQueryId, String callbackData) {
        ConversationContext context = conversationContexts.getIfPresent(chatId);
        if (context != null) {
            if (callbackData.equals("NEW_SUBMISSION") || callbackData.equals("VERIFY_ACCOUNT") || callbackData.equals("HELP")) {
                return handleMainMenuCallback(context, callbackData);
            }
            return callbackQueryHandler.handle(context, callbackQueryId, callbackData);
        }
        return messageFactory.createInvalidInputForStateMessage(chatId);
    }

    @Override
    public BotApiMethod<?> handleSubmitCommand(long chatId) {
        ConversationContext context = conversationContexts.getIfPresent(chatId);
        if (context != null) {
            BotApiMethod<?> result = context.getState().handleSubmitCommand(context);
            conversationContexts.invalidate(chatId); // Invalidate context on successful submission
            return result;
        }
        return messageFactory.createNothingToSubmitMessage(chatId);
    }

    @Override
    public BotApiMethod<?> handleSkipCommand(long chatId) {
        ConversationContext context = conversationContexts.getIfPresent(chatId);
        if (context != null) {
            return context.getState().handleSkipCommand(context);
        }
        return messageFactory.createNothingToSkipMessage(chatId);
    }

    @Override
    public BotApiMethod<?> handleContact(long chatId, Contact contact) {
        ConversationContext context = conversationContexts.getIfPresent(chatId);
        if (context != null) {
            return context.getState().handleContact(context, contact);
        }
        return messageFactory.createInvalidInputForStateMessage(chatId);
    }

    private BotApiMethod<?> handleMainMenuCallback(ConversationContext context, String callbackData) {
        long chatId = context.getChatId();
        switch (callbackData) {
            case "NEW_SUBMISSION":
                if (!telegramBotProperties.getAuth().isEnabled()) {
                    log.info("Authentication is disabled. Proceeding with anonymous user.");
                    context.setState(stateFactory.createState(ProposalStatus.IN_PROGRESS));
                    return messageFactory.createGreetingMessage(chatId);
                }

                Optional<User> userOptional = userIdentityRepository.findByProviderAndValue(Provider.TELEGRAM, String.valueOf(chatId))
                        .map(UserIdentity::getUser);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    log.info("User already authenticated: {}", user.getUsername());
                    context.setUserId(user.getId());
                    context.setState(stateFactory.createState(ProposalStatus.IN_PROGRESS));
                    return messageFactory.createWelcomeBackMessage(chatId, user.getFirstName());
                } else {
                    context.setState(stateFactory.createState(ProposalStatus.AWAITING_AUTHENTICATION));
                    return messageFactory.createAuthenticationRequestMessage(chatId);
                }
            case "VERIFY_ACCOUNT":
                context.setState(stateFactory.createState(ProposalStatus.AWAITING_AUTHENTICATION));
                return messageFactory.createAuthenticationRequestMessage(chatId);
            case "HELP":
                return messageFactory.createHelpMessage(chatId);
            default:
                return messageFactory.createUnknownCommandHelpMessage(chatId);
        }
    }
}
