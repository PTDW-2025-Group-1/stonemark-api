package pt.estga.auth.services.tfa;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.auth.dtos.TfaSetupResponseDto;
import pt.estga.user.entities.User;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.services.UserService;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
@RequiredArgsConstructor
@Slf4j
public class TotpServiceImpl implements TotpService {

    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;
    private final UserService userService;
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;

    @Override
    @Transactional
    public TfaSetupResponseDto setupTotpForUser(User user) {
        log.info("Setting up TOTP for user: {}", user.getUsername());
        String newSecret = secretGenerator.generate();
        user.setTfaSecret(newSecret);
        userService.update(user);
        log.debug("User {} TFA secret updated.", user.getUsername());
        String qrCodeImageUrl = generateQrCode(user);
        log.info("TOTP setup completed for user: {}", user.getUsername());
        return new TfaSetupResponseDto(newSecret, qrCodeImageUrl);
    }

    public String generateQrCode(User user) {
        log.debug("Generating QR code for user: {}", user.getUsername());
        QrData data = new QrData.Builder()
                .label(user.getUsername())
                .secret(user.getTfaSecret())
                .issuer("Stonemark")
                .build();
        try {
            String qrCodeUri = getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
            log.debug("QR code generated successfully for user: {}", user.getUsername());
            return qrCodeUri;
        } catch (QrGenerationException e) {
            log.error("Error generating QR code for user: {}", user.getUsername(), e);
            throw new RuntimeException("Error generating QR code", e);
        }
    }

    @Override
    public boolean isCodeValid(String secret, String code) {
        log.debug("Verifying TOTP code for secret: {} and code: {}", secret, code);
        boolean isValid = codeVerifier.isValidCode(secret, code);
        log.debug("TOTP code validation result: {}", isValid);
        return isValid;
    }

    @Override
    @Transactional
    public void enableTfa(User user, TfaMethod method) {
        log.info("Enabling TFA for user: {} with method: {}", user.getUsername(), method);
        user.setTfaMethod(method);
        userService.update(user);
        log.info("TFA enabled successfully for user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void disableTfa(User user) {
        log.info("Disabling TFA for user: {}", user.getUsername());
        user.setTfaMethod(TfaMethod.NONE);
        user.setTfaSecret(null);
        userService.update(user);
        log.info("TFA disabled successfully for user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public boolean verifyAndDisableTfa(User user, String code) {
        log.info("Attempting to verify and disable TFA for user: {}", user.getUsername());
        boolean isValid = false;
        TfaMethod tfaMethod = user.getTfaMethod();
        log.debug("User {} current TFA method is: {}", user.getUsername(), tfaMethod);

        if (tfaMethod == TfaMethod.TOTP) {
            if (user.getTfaSecret() != null) {
                isValid = isCodeValid(user.getTfaSecret(), code);
                log.debug("TOTP code verification result for user {}: {}", user.getUsername(), isValid);
            } else {
                log.warn("User {} has TOTP method set but no secret is stored.", user.getUsername());
            }
        } else if (tfaMethod == TfaMethod.SMS || tfaMethod == TfaMethod.EMAIL) {
            isValid = twoFactorAuthenticationService.verifyTfaContactCode(user, code);
            log.debug("Contact-based TFA code verification result for user {}: {}", user.getUsername(), isValid);
        } else {
            log.warn("User {} has an unsupported TFA method for verification: {}", user.getUsername(), tfaMethod);
        }

        if (isValid) {
            log.info("TFA code verified successfully for user: {}. Disabling TFA.", user.getUsername());
            disableTfa(user);
        } else {
            log.warn("TFA code verification failed for user: {}", user.getUsername());
        }
        return isValid;
    }
}
