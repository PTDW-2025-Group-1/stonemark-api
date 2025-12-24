package pt.estga.chatbots.core.shared.models.ui;

import lombok.Builder;
import lombok.Data;
import pt.estga.chatbots.core.shared.models.text.TextNode;

@Data
@Builder
public class LocationRequest implements UIComponent {
    private String message;
    private TextNode messageNode;
}
