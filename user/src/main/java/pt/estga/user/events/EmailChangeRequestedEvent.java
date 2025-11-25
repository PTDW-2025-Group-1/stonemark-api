package pt.estga.user.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pt.estga.user.entities.User;

@Getter
public class EmailChangeRequestedEvent extends ApplicationEvent {

    private final User user;
    private final String newEmail;

    public EmailChangeRequestedEvent(Object source, User user, String newEmail) {
        super(source);
        this.user = user;
        this.newEmail = newEmail;
    }
}
