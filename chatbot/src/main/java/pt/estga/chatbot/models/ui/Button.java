package pt.estga.chatbot.models.ui;

import lombok.Builder;
import lombok.Data;
import pt.estga.chatbot.models.text.TextNode;

@Data
@Builder
public class Button {
    private TextNode textNode;
    private String callbackData;
}
