package pt.estga.chatbots.core.shared.models.ui;

import lombok.Builder;
import lombok.Data;
import pt.estga.chatbots.core.shared.models.text.TextNode;

@Data
@Builder
public class PhotoItem implements UIComponent {
    private Long mediaFileId;
    private String caption;
    private TextNode captionNode;
    private String callbackData;
}
