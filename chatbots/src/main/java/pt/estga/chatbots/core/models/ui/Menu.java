package pt.estga.chatbots.core.models.ui;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Menu implements UIComponent {
    private String title;
    private List<List<Button>> buttons;
}
