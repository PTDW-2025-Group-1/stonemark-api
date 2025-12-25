package pt.estga.chatbots.models.ui;

import lombok.Builder;
import lombok.Data;
import pt.estga.chatbots.models.text.TextNode;

@Data
@Builder
public class PhotoItem implements UIComponent {
    private Long mediaFileId;
    private TextNode captionNode;
    private String callbackData;
}
