package pt.estga.chatbots.models.text;

public sealed interface TextNode
        permits Plain, Bold, Italic, Code, NewLine, Container {
}