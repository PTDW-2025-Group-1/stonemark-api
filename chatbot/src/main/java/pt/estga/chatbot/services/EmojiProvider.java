package pt.estga.chatbot.services;

import pt.estga.chatbot.constants.EmojiKey;

public interface EmojiProvider {
    String render(EmojiKey key);
}
