package pt.estga.chatbot.models.text;

import java.util.List;

public record Italic(List<TextNode> children) implements TextNode {
}