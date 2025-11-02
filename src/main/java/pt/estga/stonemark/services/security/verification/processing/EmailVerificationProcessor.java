package pt.estga.stonemark.services.security.verification.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.services.UserService;

@Component
@RequiredArgsConstructor
public class EmailVerificationProcessor implements VerificationProcessor {

    private final UserService userService;

    @Override
    public void process(VerificationToken token) {
        User user = token.getUser();
        user.setEnabled(true);
        userService.update(user);
    }

    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.EMAIL_VERIFICATION;
    }
}
