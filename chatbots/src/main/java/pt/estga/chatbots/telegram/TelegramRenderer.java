package pt.estga.chatbots.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.models.text.*;
import pt.estga.chatbots.core.shared.services.TextRenderer;

@Component
@Slf4j
public class TelegramRenderer implements TextRenderer {

    @Override
    public RenderedText render(TextNode node) {
        boolean hasFormatting = containsFormatting(node);
        String text = renderNode(node, hasFormatting);

        String parseMode = hasFormatting ? "MarkdownV2" : null;
        return new RenderedText(text, parseMode);
    }

    private String renderNode(TextNode node, boolean escapeContent) {
        return switch (node) {
            case Plain p -> escapeContent ? escape(p.text()) : p.text();
            case Bold b -> "*" + renderChildren(b.children(), escapeContent) + "*";
            case Italic i -> "_" + renderChildren(i.children(), escapeContent) + "_";
            case Code c -> "`" + escapeCode(c.text()) + "`";
            case NewLine ignored -> "\n";
            case Container c -> renderChildren(c.children(), escapeContent);
        };
    }

    private boolean containsFormatting(TextNode node) {
        if (node instanceof Bold || node instanceof Italic || node instanceof Code) {
            return true;
        } else if (node instanceof Container(java.util.List<TextNode> children)) {
            for (TextNode child : children) {
                if (containsFormatting(child)) return true;
            }
        }
        return false;
    }

    private String renderChildren(Iterable<TextNode> children, boolean escapeContent) {
        StringBuilder sb = new StringBuilder();
        for (TextNode child : children) {
            sb.append(renderNode(child, escapeContent));
        }
        return sb.toString();
    }

    /**
     * Escapes characters for MarkdownV2.
     * This method iterates over the string character by character to ensure that
     * surrogate pairs (e.g. emojis) are preserved correctly and not accidentally
     * matched or broken.
     */
    private String escape(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (shouldEscape(c)) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private boolean shouldEscape(char c) {
        return c == '\\' || c == '_' || c == '*' || c == '[' || c == ']' || c == '(' || c == ')' || c == '~' || c == '`' || c == '>' || c == '#' || c == '+' || c == '-' || c == '=' || c == '|' || c == '{' || c == '}' || c == '.' || c == '!';
    }

    private String escapeCode(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("`", "\\`");
    }
}
