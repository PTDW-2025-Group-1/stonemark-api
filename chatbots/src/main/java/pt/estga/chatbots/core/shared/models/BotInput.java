package pt.estga.chatbots.core.shared.models;

import lombok.Builder;
import lombok.Data;
import pt.estga.shared.models.Location;

@Data
@Builder
public class BotInput {
    private String userId;
    private long chatId; // Added to consolidate all input info
    private String platform;
    private InputType type;
    private String text;
    private byte[] fileData;
    private String fileName;
    private Location location;
    private String callbackData;

    public enum InputType {
        TEXT,
        COMMAND,
        PHOTO,
        LOCATION,
        CALLBACK,
        CONTACT
    }
}
