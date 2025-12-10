package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import pt.estga.auth.entities.ActionCode;
import pt.estga.auth.enums.ActionCodeType;
import pt.estga.auth.services.ActionCodeService;
import pt.estga.auth.services.verification.VerificationDispatchService;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;

@RequiredArgsConstructor
public class ActionCodeCommand implements VerificationCommand<Void> {

    private final User user;
    private final UserContact userContact;
    private final ActionCodeService actionCodeService;
    private final VerificationDispatchService verificationDispatchService;
    private final ActionCodeType actionCodeType;

    @Override
    public void execute(Void parameter) {
        ActionCode actionCode = actionCodeService.createAndSave(user, actionCodeType);
        verificationDispatchService.sendVerification(userContact, actionCode);
    }
}
