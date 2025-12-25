package pt.estga.chatbot.models.text;

import java.util.List;

public record Bold(List<TextNode> children) implements TextNode {}