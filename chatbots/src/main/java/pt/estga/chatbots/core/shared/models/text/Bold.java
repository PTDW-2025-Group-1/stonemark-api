package pt.estga.chatbots.core.shared.models.text;

import java.util.List;

public record Bold(List<TextNode> children) implements TextNode {
}