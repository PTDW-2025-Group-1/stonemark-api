package pt.estga.user.dtos;

import lombok.Builder;
import lombok.Data;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;

@Data
@Builder
// Todo: pass to record
public class UserContactDto {
    private Long id;
    private String value;
    private ContactType type;
    private boolean isPrimary;
    private boolean isVerified;

    public static UserContactDto fromEntity(UserContact userContact) {
        return UserContactDto.builder()
                .id(userContact.getId())
                .value(userContact.getValue())
                .type(userContact.getType())
                .isPrimary(userContact.isPrimary())
                .isVerified(userContact.isVerified())
                .build();
    }
}
