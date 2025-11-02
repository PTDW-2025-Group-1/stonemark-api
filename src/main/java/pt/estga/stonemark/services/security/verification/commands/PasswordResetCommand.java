package pt.estga.stonemark.services.security.verification.commands;

import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.request.PasswordResetRequest;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.repositories.PasswordResetRequestRepository;
import pt.estga.stonemark.services.security.token.VerificationTokenService;
import pt.estga.stonemark.services.security.verification.VerificationEmailService;

public class PasswordResetCommand implements VerificationCommand {

    private final User user;
    private final String newPassword;
    private final VerificationTokenService verificationTokenService;
    private final VerificationEmailService verificationEmailService;
    private final PasswordResetRequestRepository passwordResetRequestRepository;

    public PasswordResetCommand(User user, String newPassword, VerificationTokenService verificationTokenService, VerificationEmailService verificationEmailService, PasswordResetRequestRepository passwordResetRequestRepository) {
        this.user = user;
        this.newPassword = newPassword;
        this.verificationTokenService = verificationTokenService;
        this.verificationEmailService = verificationEmailService;
        this.passwordResetRequestRepository = passwordResetRequestRepository;
    }

    @Override
    public void execute() {
        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.PASSWORD_RESET);

        PasswordResetRequest passwordResetRequest = PasswordResetRequest.builder()
                .user(user)
                .newPassword(newPassword)
                .verificationToken(verificationToken)
                .build();

        passwordResetRequestRepository.save(passwordResetRequest);

        verificationEmailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }
}
