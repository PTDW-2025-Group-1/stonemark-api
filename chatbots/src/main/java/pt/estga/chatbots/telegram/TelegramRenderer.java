package pt.estga.chatbots.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.models.text.*;
import pt.estga.chatbots.core.shared.services.TextRenderer;

import java.util.stream.Collectors;

@Component
@Slf4j
public class TelegramRenderer implements TextRenderer {

    @Override
    public String render(TextNode node) {
        log.debug("Rendering node: {}", node);
        String result = switch (node) {
            case Plain p -> escape(p.text());
            case Bold b -> "*" + renderChildren(b.children()) + "*";
            case Italic i -> "_" + renderChildren(i.children()) + "_";
            case Code c -> "`" + escape(c.text()) + "`";
            case NewLine ignored -> "\n";
            case Container c -> renderChildren(c.children());
        };
        log.debug("Rendered result: {}", result);
        return result;
    }

    private String renderChildren(Iterable<TextNode> nodes) {
        return stream(nodes)
                .map(this::render)
                .collect(Collectors.joining());
    }

    private String escape(String text) {
        String escaped = text
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("`", "\\`");
        log.trace("Escaped text '{}' to '{}'", text, escaped);
        return escaped;
    }

    private java.util.stream.Stream<TextNode> stream(Iterable<TextNode> it) {
        return it instanceof java.util.Collection<?>
                ? ((java.util.Collection<TextNode>) it).stream()
                : java.util.stream.StreamSupport.stream(it.spliterator(), false);
    }
}
