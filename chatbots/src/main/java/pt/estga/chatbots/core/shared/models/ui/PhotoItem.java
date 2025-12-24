package pt.estga.chatbots.core.shared.models.ui;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhotoItem implements UIComponent {
    private Long mediaFileId;
    private String caption;
    private String callbackData;
}
