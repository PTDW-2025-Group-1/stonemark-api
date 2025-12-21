package pt.estga.chatbots.core.models;

import lombok.Builder;
import lombok.Data;
import pt.estga.chatbots.core.models.ui.UIComponent;

@Data
@Builder
public class BotResponse {
    private UIComponent uiComponent;
}
