package pt.estga.chatbots.models.text;

import java.util.List;

public record Container(List<TextNode> children) implements TextNode {
}