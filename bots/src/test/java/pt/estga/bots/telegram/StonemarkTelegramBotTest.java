package pt.estga.bots.telegram;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import pt.estga.bots.telegram.services.TelegramBotCommandService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class StonemarkTelegramBotTest {

    @Mock
    private TelegramBotCommandService commandService;

    private StonemarkTelegramBot bot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bot = new StonemarkTelegramBot("testbot", "testtoken", "/testbot", commandService);
    }

    @Test
    void testStartCommand() {
        long chatId = 123L;
        Update update = createTextUpdate(chatId, "/start");
        BotApiMethod<?> expectedResponse = new SendMessage(String.valueOf(chatId), "Welcome!");

        doReturn(expectedResponse).when(commandService).handleStartCommand(chatId);

        BotApiMethod<?> response = bot.onWebhookUpdateReceived(update);

        assertEquals(expectedResponse, response);
        verify(commandService).handleStartCommand(chatId);
    }

    @Test
    void testTextMessage() {
        long chatId = 123L;
        String text = "Some text";
        Update update = createTextUpdate(chatId, text);
        BotApiMethod<?> expectedResponse = new SendMessage(String.valueOf(chatId), "Text received");

        doReturn(expectedResponse).when(commandService).handleTextMessage(chatId, text);

        BotApiMethod<?> response = bot.onWebhookUpdateReceived(update);

        assertEquals(expectedResponse, response);
        verify(commandService).handleTextMessage(chatId, text);
    }

    private Update createTextUpdate(long chatId, String text) {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(chatId);
        message.setChat(chat);
        message.setText(text);
        update.setMessage(message);
        return update;
    }
}
