package pt.estga.stonemark.services.security.verification.commands;

import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.request.EmailChangeRequest;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.exceptions.EmailAlreadyTakenException;
import pt.estga.stonemark.repositories.EmailChangeRequestRepository;
import pt.estga.stonemark.services.UserService;
import pt.estga.stonemark.services.security.token.VerificationTokenService;
import pt.estga.stonemark.services.security.verification.VerificationEmailService;

public class EmailChangeCommand implements VerificationCommand {

    private final User user;
    private final String newEmail;
    private final VerificationTokenService verificationTokenService;
    private final VerificationEmailService verificationEmailService;
    private final EmailChangeRequestRepository emailChangeRequestRepository;
    private final UserService userService;

    public EmailChangeCommand(User user, String newEmail, VerificationTokenService verificationTokenService, VerificationEmailService verificationEmailService, EmailChangeRequestRepository emailChangeRequestRepository, UserService userService) {
        this.user = user;
        this.newEmail = newEmail;
        this.verificationTokenService = verificationTokenService;
        this.verificationEmailService = verificationEmailService;
        this.emailChangeRequestRepository = emailChangeRequestRepository;
        this.userService = userService;
    }

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

        verificationEmailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }
}
