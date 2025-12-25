package pt.estga.chatbots.telegram.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.models.text.RenderedText;
import pt.estga.chatbots.models.text.TextNode;
import pt.estga.chatbots.telegram.TelegramRenderer;

@Component
@RequiredArgsConstructor
public class TelegramTextService {

    private final TelegramRenderer renderer;

    public RenderedText render(TextNode node) {
        return renderer.render(node);
    }
}
