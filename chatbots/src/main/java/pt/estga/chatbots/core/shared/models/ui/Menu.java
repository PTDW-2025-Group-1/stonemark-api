package pt.estga.chatbots.core.shared.models.ui;

import lombok.Builder;
import lombok.Data;
import pt.estga.chatbots.core.shared.models.text.TextNode;

import java.util.List;

@Data
@Builder
public class Menu implements UIComponent {
    private String title;
    private TextNode titleNode;
    private List<List<Button>> buttons;
}
