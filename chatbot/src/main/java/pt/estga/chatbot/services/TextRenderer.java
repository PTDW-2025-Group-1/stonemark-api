package pt.estga.chatbot.services;

import pt.estga.chatbot.models.text.RenderedText;
import pt.estga.chatbot.models.text.TextNode;

public interface TextRenderer {
    RenderedText render(TextNode node);
}
