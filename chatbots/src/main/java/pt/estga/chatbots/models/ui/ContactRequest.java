package pt.estga.chatbots.models.ui;

import lombok.Builder;
import lombok.Data;
import pt.estga.chatbots.models.text.TextNode;

@Data
@Builder
public class ContactRequest implements UIComponent {
    private TextNode messageNode;
}
