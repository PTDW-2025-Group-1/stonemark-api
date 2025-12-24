package pt.estga.chatbots.core.shared.models.text;

import java.util.List;

public record Italic(List<TextNode> children) implements TextNode {
}