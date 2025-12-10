package pt.estga.verification.services.commands;

import lombok.RequiredArgsConstructor;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;
import pt.estga.verification.services.ActionCodeService;
import pt.estga.verification.services.VerificationDispatchService;

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
