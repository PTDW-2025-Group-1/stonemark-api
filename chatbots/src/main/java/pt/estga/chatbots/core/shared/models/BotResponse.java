package pt.estga.chatbots.core.shared.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import pt.estga.chatbots.core.shared.models.text.TextNode;
import pt.estga.chatbots.core.shared.models.ui.UIComponent;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BotResponse {
    private String text;
    private TextNode textNode;
    private UIComponent uiComponent;
}
