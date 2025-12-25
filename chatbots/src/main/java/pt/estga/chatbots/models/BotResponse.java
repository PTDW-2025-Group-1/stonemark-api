package pt.estga.chatbots.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import pt.estga.chatbots.models.text.TextNode;
import pt.estga.chatbots.models.ui.UIComponent;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BotResponse {
    private TextNode textNode;
    private UIComponent uiComponent;
}
