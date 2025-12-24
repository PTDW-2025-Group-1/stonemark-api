package pt.estga.chatbots.core.shared.models.ui;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import pt.estga.chatbots.core.shared.models.text.TextNode;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class TextMessage implements UIComponent {
    private TextNode textNode;
}
