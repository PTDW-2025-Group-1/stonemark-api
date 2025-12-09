package pt.estga.user.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;

@Getter
public class EmailVerificationRequestedEvent extends ApplicationEvent {

    private final User user;
    private final UserContact userContact; // Added userContact field

    public EmailVerificationRequestedEvent(Object source, User user, UserContact userContact) { // Updated constructor
        super(source);
        this.user = user;
        this.userContact = userContact; // Initialize userContact
    }
}
