package pt.estga.chatbots.models.ui;

import lombok.Builder;
import lombok.Data;
import pt.estga.chatbots.models.text.TextNode;

@Data
@Builder
public class Button {
    private TextNode textNode;
    private String callbackData;
}
