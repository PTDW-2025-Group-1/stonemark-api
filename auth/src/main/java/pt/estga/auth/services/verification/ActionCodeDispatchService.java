package pt.estga.auth.services.verification;

import pt.estga.auth.entities.ActionCode;
import pt.estga.user.entities.UserContact;

public interface ActionCodeDispatchService {

    void sendVerification(UserContact userContact, ActionCode code);

}
