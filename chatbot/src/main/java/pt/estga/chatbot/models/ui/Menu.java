package pt.estga.chatbot.models.ui;

import lombok.Builder;
import lombok.Data;
import pt.estga.chatbot.models.text.TextNode;

import java.util.List;

@Data
@Builder
public class Menu implements UIComponent {
    private TextNode titleNode;
    private List<List<Button>> buttons;
}
