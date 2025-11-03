package pt.estga.stonemark.services.security.verification.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.services.UserService;
import pt.estga.stonemark.services.security.token.VerificationTokenService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailVerificationProcessor implements VerificationProcessor {

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;

    @Override
    public Optional<String> process(VerificationToken token) {
        User user = token.getUser();
        user.setEnabled(true);
        userService.update(user);
        verificationTokenService.revokeToken(token);
        return Optional.empty();
    }

    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.EMAIL_VERIFICATION;
    }
}
