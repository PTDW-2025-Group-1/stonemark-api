package pt.estga.chatbot.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import pt.estga.chatbot.models.text.TextNode;
import pt.estga.chatbot.models.ui.UIComponent;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BotResponse {
    private TextNode textNode;
    private UIComponent uiComponent;
}
