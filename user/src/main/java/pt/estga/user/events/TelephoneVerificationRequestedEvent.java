package pt.estga.user.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pt.estga.user.entities.User;

@Getter
public class TelephoneVerificationRequestedEvent extends ApplicationEvent {

    private final User user;

    public TelephoneVerificationRequestedEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
