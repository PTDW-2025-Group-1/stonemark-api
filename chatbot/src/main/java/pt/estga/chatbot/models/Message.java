package pt.estga.chatbot.models;

import lombok.Getter;

@Getter
public class Message {
    private final String key;
    private final Object[] args;

    public Message(String key, Object... args) {
        this.key = key;
        this.args = args;
    }
}
