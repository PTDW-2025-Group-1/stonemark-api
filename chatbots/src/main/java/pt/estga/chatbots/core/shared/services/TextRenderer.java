package pt.estga.chatbots.core.shared.services;

import pt.estga.chatbots.core.shared.models.text.RenderedText;
import pt.estga.chatbots.core.shared.models.text.TextNode;

public interface TextRenderer {
    RenderedText render(TextNode node);
}
