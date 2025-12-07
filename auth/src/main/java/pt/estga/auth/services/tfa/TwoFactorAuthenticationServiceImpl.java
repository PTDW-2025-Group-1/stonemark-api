package pt.estga.auth.services.tfa;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.estga.user.entities.User;
import pt.estga.user.service.UserService;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService {

    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;
    private final UserService userService;

    @Override
    public String generateNewSecret() {
        return secretGenerator.generate();
    }

    @Override
    public String generateQrCode(User user) {
        QrData data = new QrData.Builder()
                .label(user.getUsername())
                .secret(user.getTfaSecret())
                .issuer("Stonemark")
                .build();
        try {
            return getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
        } catch (QrGenerationException e) {
            log.error("Error generating QR code for user: {}", user.getUsername(), e);
            throw new RuntimeException("Error generating QR code", e);
        }
    }

    @Override
    public boolean isCodeValid(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }

    @Override
    public void enableTfa(User user) {
        user.setTfaEnabled(true);
        userService.update(user);
    }

    @Override
    public void disableTfa(User user) {
        user.setTfaEnabled(false);
        user.setTfaSecret(null);
        userService.update(user);
    }
}
