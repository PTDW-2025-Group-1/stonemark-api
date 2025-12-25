package pt.estga.chatbot.models.ui;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import pt.estga.chatbot.models.text.TextNode;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class TextMessage implements UIComponent {
    private TextNode textNode;
}
