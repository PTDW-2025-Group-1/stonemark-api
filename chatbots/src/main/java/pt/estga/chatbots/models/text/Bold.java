package pt.estga.chatbots.models.text;

import java.util.List;

public record Bold(List<TextNode> children) implements TextNode {
}