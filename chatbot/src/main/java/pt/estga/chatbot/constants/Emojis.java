package pt.estga.chatbot.constants;

import lombok.Getter;

@Getter
public enum Emojis {
    WAVE("👋"),
    WARNING("⚠️"),
    CAMERA("📷"),
    LOCATION("📍"),
    PAPERCLIP("📎"),
    TRASH("🗑️"),
    SEARCH("🔍"),
    NEW("🆕"),
    MEMO("📝"),
    MONUMENT("🏛️"),
    REFRESH("🔄"),
    BACK("🔙"),
    TADA("🎉"),
    CHECK("✅"),
    CROSS("❌"),
    LOCK("🔒"),
    KEY("🔑"),
    PHONE("📱"),
    NUMBERS("🔢"),
    ARROW_RIGHT("➡️"),
    THINKING("🤔");

    private final String unicode;

    Emojis(String unicode) {
        this.unicode = unicode;
    }

    @Override
    public String toString() {
        return unicode;
    }
}
