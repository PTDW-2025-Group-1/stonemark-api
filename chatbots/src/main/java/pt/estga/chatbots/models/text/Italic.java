package pt.estga.chatbots.models.text;

import java.util.List;

public record Italic(List<TextNode> children) implements TextNode {
}