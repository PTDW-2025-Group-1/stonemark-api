package pt.estga.chatbot.models.text;

import java.util.List;

public record Container(List<TextNode> children) implements TextNode {}