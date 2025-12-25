package pt.estga.chatbot.models.text;

public sealed interface TextNode
        permits Plain, Bold, Placeholder, Italic, Code, NewLine, Container, Emoji {
}