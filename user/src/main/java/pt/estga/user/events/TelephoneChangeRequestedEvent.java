package pt.estga.user.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pt.estga.user.entities.User;

@Getter
public class TelephoneChangeRequestedEvent extends ApplicationEvent {

    private final User user;
    private final String newTelephone;

    public TelephoneChangeRequestedEvent(Object source, User user, String newTelephone) {
        super(source);
        this.user = user;
        this.newTelephone = newTelephone;
    }
}
