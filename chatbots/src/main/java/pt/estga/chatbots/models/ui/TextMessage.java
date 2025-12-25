package pt.estga.chatbots.models.ui;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import pt.estga.chatbots.models.text.TextNode;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class TextMessage implements UIComponent {
    private TextNode textNode;
}
