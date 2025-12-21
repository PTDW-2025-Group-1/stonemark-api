package pt.estga.chatbots.core.shared.models.ui;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class TextMessage implements UIComponent {
    private String text;
}
