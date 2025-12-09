package pt.estga.user.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;

@Getter
public class TelephoneVerificationRequestedEvent extends ApplicationEvent {

    private final User user;
    private final UserContact userContact;

    public TelephoneVerificationRequestedEvent(Object source, User user, UserContact userContact) {
        super(source);
        this.user = user;
        this.userContact = userContact;
    }
}
