package pt.estga.bots.telegram;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import pt.estga.bots.telegram.callback.CallbackQueryHandler;
import pt.estga.bots.telegram.context.ConversationContext;
import pt.estga.bots.telegram.message.TelegramBotMessageFactory;
import pt.estga.bots.telegram.state.factory.StateFactory;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.shared.models.Location;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotCommandServiceImpl implements TelegramBotCommandService {

    private final TelegramBotMessageFactory messageFactory;
    private final StateFactory stateFactory;
    private final CallbackQueryHandler callbackQueryHandler;

    private final Cache<Long, ConversationContext> conversationContexts = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES) // Evict entries after 30 minutes of inactivity
            .maximumSize(10_000) // Maximum number of entries in the cache
            .build();

    @Override
    public BotApiMethod<?> handleStartCommand(long chatId) {
        ConversationContext context = new ConversationContext(chatId);
        context.setState(stateFactory.createState(ProposalStatus.IN_PROGRESS));
        conversationContexts.put(chatId, context);
        return messageFactory.createGreetingMessage(chatId);
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
        return callbackQueryHandler.handle(context, callbackQueryId, callbackData);
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
}
