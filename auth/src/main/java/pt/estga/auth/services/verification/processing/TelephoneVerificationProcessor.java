package pt.estga.auth.services.verification.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.user.entities.User;
import pt.estga.user.service.UserService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TelephoneVerificationProcessor implements VerificationProcessor {

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
        return VerificationTokenPurpose.TELEPHONE_VERIFICATION;
    }
}
