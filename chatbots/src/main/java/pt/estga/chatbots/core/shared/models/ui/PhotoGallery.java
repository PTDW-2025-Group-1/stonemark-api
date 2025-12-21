package pt.estga.chatbots.core.shared.models.ui;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PhotoGallery implements UIComponent {
    private String title;
    private List<PhotoItem> photos;
    private List<List<Button>> additionalButtons;
    
    @Data
    @Builder
    public static class PhotoItem {
        private String imageUrl;
        private String caption;
        private String callbackData;
    }
}
