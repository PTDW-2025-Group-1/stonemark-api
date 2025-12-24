package pt.estga.chatbots.core.shared.models.ui;

import lombok.Builder;
import lombok.Data;
import pt.estga.chatbots.core.shared.models.text.TextNode;

@Data
@Builder
public class Button {
    private TextNode textNode;
    private String callbackData;
}
