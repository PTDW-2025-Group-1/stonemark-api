package pt.estga.chatbots.core.shared.models.ui;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Button {
    private String text;
    private String callbackData;
}
