package pt.estga.chatbots.core.shared.models.text;

import java.util.List;

public record Container(List<TextNode> children) implements TextNode {
}