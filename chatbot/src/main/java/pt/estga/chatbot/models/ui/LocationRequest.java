package pt.estga.chatbot.models.ui;

import lombok.Builder;
import lombok.Data;
import pt.estga.chatbot.models.text.TextNode;

@Data
@Builder
public class LocationRequest implements UIComponent {
    private TextNode messageNode;
}
