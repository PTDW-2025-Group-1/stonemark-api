package pt.estga.stonemark.bots.telegram;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.function.Function;

public class StonemarkTelegramBot extends TelegramWebhookBot {

    private final String botUsername;
    private final String botPath;

    private final Map<String, Function<Update, BotApiMethod<?>>> commandRegistry;

    public StonemarkTelegramBot(String botUsername, String botToken, String botPath) {
        super(botToken);
        this.botUsername = botUsername;
        this.botPath = botPath;

        // Register bot commands
        this.commandRegistry = Map.of(
                "/start", this::handleStartCommand,
                "/help", this::handleHelpCommand,
                "/demo", this::handleDemoCommand
        );
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            if (messageText.startsWith("/")) {
                String command = messageText.split(" ")[0];
                Function<Update, BotApiMethod<?>> action = commandRegistry.get(command);

                if (action != null) {
                    return action.apply(update);
                } else {
                    return handleUnknownCommand(update);
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> handleStartCommand(Update update) {
        SendMessage response = new SendMessage();
        response.setChatId(update.getMessage().getChatId().toString());
        response.setText("Welcome to the Stonemark Bot!");
        return response;
    }

    private BotApiMethod<?> handleHelpCommand(Update update) {
        SendMessage response = new SendMessage();
        response.setChatId(update.getMessage().getChatId().toString());
        response.setText("Available commands:\n/start - Welcome message\n/help - Show this message\n/demo - Show a demo");
        return response;
    }

    private BotApiMethod<?> handleDemoCommand(Update update) {
        SendMessage response = new SendMessage();
        response.setChatId(update.getMessage().getChatId().toString());
        response.setText("This is a demo command.");
        return response;
    }

    private BotApiMethod<?> handleUnknownCommand(Update update) {
        SendMessage response = new SendMessage();
        response.setChatId(update.getMessage().getChatId().toString());
        response.setText("Sorry, I don't recognize that command.");
        return response;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }
}
