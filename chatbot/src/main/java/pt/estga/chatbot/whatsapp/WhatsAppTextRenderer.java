package pt.estga.chatbot.whatsapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.models.text.*;
import pt.estga.chatbot.services.EmojiProvider;
import pt.estga.chatbot.services.TextRenderer;

@Component
@Slf4j
public class WhatsAppTextRenderer implements TextRenderer {

    private final EmojiProvider emojiProvider;

    public WhatsAppTextRenderer(@Qualifier("whatsappEmojiProvider") EmojiProvider emojiProvider) {
        this.emojiProvider = emojiProvider;
    }

    @Override
    public RenderedText render(TextNode node) {
        // WhatsApp doesn't have a specific "parse mode" like Telegram's MarkdownV2 vs HTML
        // It just supports basic formatting.
        String text = renderNode(node);
        return new RenderedText(text, null);
    }

    private String renderNode(TextNode node) {
        return switch (node) {
            case Plain p -> p.text();
            case Bold b -> "*" + renderChildren(b.children()) + "*";
            case Italic i -> "_" + renderChildren(i.children()) + "_";
            case Code c -> "```" + c.text() + "```";
            case Emoji e -> emojiProvider.render(e.key());
            case Placeholder p -> "{" + p.index() + "}";
            case NewLine ignored -> "\n";
            case Container c -> renderChildren(c.children());
        };
    }

    private String renderChildren(Iterable<TextNode> children) {
        StringBuilder sb = new StringBuilder();
        for (TextNode child : children) {
            sb.append(renderNode(child));
        }
        return sb.toString();
    }
}
