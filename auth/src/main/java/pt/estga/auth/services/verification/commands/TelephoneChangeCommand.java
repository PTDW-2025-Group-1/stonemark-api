package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import pt.estga.auth.entities.request.TelephoneChangeRequest;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.repositories.TelephoneChangeRequestRepository;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.sms.SmsVerificationService;
import pt.estga.shared.exceptions.TelephoneAlreadyTakenException;
import pt.estga.user.entities.User;
import pt.estga.user.service.UserService;

@RequiredArgsConstructor
public class TelephoneChangeCommand implements VerificationCommand {

    private final User user;
    private final String newTelephone;
    private final VerificationTokenService verificationTokenService;
    private final SmsVerificationService smsVerificationService;
    private final TelephoneChangeRequestRepository telephoneChangeRequestRepository;
    private final UserService userService;

    @Override
    public void execute() {
        if (userService.existsByTelephone(newTelephone)) {
            throw new TelephoneAlreadyTakenException("Telephone is already taken.");
        }

        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.TELEPHONE_CHANGE_REQUEST);

        TelephoneChangeRequest telephoneChangeRequest = TelephoneChangeRequest.builder()
                .user(user)
                .newTelephone(newTelephone)
                .verificationToken(verificationToken)
                .build();

        telephoneChangeRequestRepository.save(telephoneChangeRequest);

        smsVerificationService.sendVerificationSms(user.getTelephone(), verificationToken);
    }
}
