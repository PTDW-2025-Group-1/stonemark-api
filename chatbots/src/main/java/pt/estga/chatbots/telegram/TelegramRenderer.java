package pt.estga.chatbots.telegram;

import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.models.text.*;
import pt.estga.chatbots.core.shared.services.TextRenderer;

import java.util.stream.Collectors;

@Component
public class TelegramRenderer implements TextRenderer {

    @Override
    public String render(TextNode node) {
        return switch (node) {
            case Plain p -> escape(p.text());
            case Bold b -> "*" + renderChildren(b.children()) + "*";
            case Italic i -> "_" + renderChildren(i.children()) + "_";
            case Code c -> "`" + escape(c.text()) + "`";
            case NewLine ignored -> "\n";
            case Container c -> renderChildren(c.children());
        };
    }

    private String renderChildren(Iterable<TextNode> nodes) {
        return stream(nodes)
                .map(this::render)
                .collect(Collectors.joining());
    }

    private String escape(String text) {
        return text
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("`", "\\`");
    }

    private java.util.stream.Stream<TextNode> stream(Iterable<TextNode> it) {
        return it instanceof java.util.Collection<?>
                ? ((java.util.Collection<TextNode>) it).stream()
                : java.util.stream.StreamSupport.stream(it.spliterator(), false);
    }
}
