package pt.estga.chatbots.core.shared.services;

import pt.estga.chatbots.core.shared.models.text.TextNode;

public interface TextRenderer {
    String render(TextNode node);
}
