package pt.estga.chatbots.services;

import pt.estga.chatbots.models.text.RenderedText;
import pt.estga.chatbots.models.text.TextNode;

public interface TextRenderer {
    RenderedText render(TextNode node);
}
