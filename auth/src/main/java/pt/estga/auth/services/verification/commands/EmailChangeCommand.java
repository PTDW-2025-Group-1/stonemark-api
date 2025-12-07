package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import pt.estga.auth.entities.request.EmailChangeRequest;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.repositories.EmailChangeRequestRepository;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.email.EmailVerificationService;
import pt.estga.shared.exceptions.EmailAlreadyTakenException;
import pt.estga.user.entities.User;
import pt.estga.user.service.UserService;

@RequiredArgsConstructor
public class EmailChangeCommand implements VerificationCommand {

    private final User user;
    private final String newEmail;
    private final VerificationTokenService verificationTokenService;
    private final EmailVerificationService emailVerificationService;
    private final EmailChangeRequestRepository emailChangeRequestRepository;
    private final UserService userService;

    @Override
    public void execute() {
        if (userService.existsByEmail(newEmail)) {
            throw new EmailAlreadyTakenException("Email is already taken.");
        }

        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.EMAIL_CHANGE_REQUEST);

        EmailChangeRequest emailChangeRequest = EmailChangeRequest.builder()
                .user(user)
                .newEmail(newEmail)
                .verificationToken(verificationToken)
                .build();

        emailChangeRequestRepository.save(emailChangeRequest);

        userService.getPrimaryEmail(user).ifPresent(primaryEmail ->
                emailVerificationService.sendVerificationEmail(primaryEmail, verificationToken)
        );
    }
}
