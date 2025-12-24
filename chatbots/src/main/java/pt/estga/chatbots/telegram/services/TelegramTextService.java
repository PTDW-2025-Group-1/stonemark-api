package pt.estga.chatbots.telegram.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.models.text.TextNode;
import pt.estga.chatbots.telegram.TelegramRenderer;

@Component
@RequiredArgsConstructor
public class TelegramTextService {

    private final TelegramRenderer renderer;

    public String render(TextNode node) {
        return renderer.render(node);
    }
}
