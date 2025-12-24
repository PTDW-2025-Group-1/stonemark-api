package pt.estga.chatbots.core.shared.models.text;

public sealed interface TextNode
        permits Plain, Bold, Italic, Code, NewLine, Container {
}