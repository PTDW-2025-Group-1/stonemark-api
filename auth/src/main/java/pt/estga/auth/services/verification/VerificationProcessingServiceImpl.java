package pt.estga.auth.services.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.stonemark.dtos.auth.ConfirmationResponseDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.exceptions.InvalidTokenException;
import pt.estga.stonemark.exceptions.SamePasswordException;
import pt.estga.stonemark.services.user.UserService;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.processing.VerificationProcessor;
import pt.estga.auth.services.verification.processing.VerificationProcessorFactory;
import pt.estga.stonemark.exceptions.VerificationErrorMessages;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationProcessingServiceImpl implements VerificationProcessingService {

    private final VerificationTokenService verificationTokenService;
    private final VerificationProcessorFactory verificationProcessorFactory;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public ConfirmationResponseDto processTokenConfirmation(String token) {
        try {
            VerificationToken vt = verificationTokenService.findByToken(token)
                    .orElseThrow(() -> new InvalidTokenException(VerificationErrorMessages.TOKEN_NOT_FOUND));

            if (vt.getExpiresAt().isBefore(Instant.now())) {
                verificationTokenService.revokeToken(vt);
                throw new InvalidTokenException(VerificationErrorMessages.TOKEN_EXPIRED);
            }

            if (vt.isRevoked()) {
                throw new InvalidTokenException(VerificationErrorMessages.TOKEN_REVOKED);
            }

            VerificationProcessor processor = verificationProcessorFactory.getProcessor(vt.getPurpose());
            Optional<String> resultToken = processor.process(vt);

            return resultToken.map(ConfirmationResponseDto::passwordResetRequired)
                    .orElseGet(() -> ConfirmationResponseDto.success(VerificationErrorMessages.CONFIRMATION_SUCCESSFUL));
        } catch (InvalidTokenException e) {
            return ConfirmationResponseDto.error(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ConfirmationResponseDto.error(VerificationErrorMessages.INVALID_TOKEN_PURPOSE + e.getMessage());
        }
    }

    @Transactional
    @Override
    public ConfirmationResponseDto processCodeConfirmation(String code) {
        try {
            VerificationToken vt = verificationTokenService.findByCode(code)
                    .orElseThrow(() -> new InvalidTokenException(VerificationErrorMessages.CODE_NOT_FOUND));

            if (vt.getExpiresAt().isBefore(Instant.now())) {
                verificationTokenService.revokeToken(vt);
                throw new InvalidTokenException(VerificationErrorMessages.CODE_EXPIRED);
            }

            if (vt.isRevoked()) {
                throw new InvalidTokenException(VerificationErrorMessages.CODE_REVOKED);
            }

            VerificationProcessor processor = verificationProcessorFactory.getProcessor(vt.getPurpose());
            Optional<String> resultToken = processor.process(vt);

            return resultToken.map(ConfirmationResponseDto::passwordResetRequired)
                    .orElseGet(() -> ConfirmationResponseDto.success(VerificationErrorMessages.CONFIRMATION_SUCCESSFUL));
        } catch (InvalidTokenException e) {
            return ConfirmationResponseDto.error(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ConfirmationResponseDto.error(VerificationErrorMessages.INVALID_CODE_PURPOSE + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void processPasswordReset(String token, String newPassword) {
        VerificationToken vt = verificationTokenService.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException(VerificationErrorMessages.TOKEN_NOT_FOUND));

        if (vt.getExpiresAt().isBefore(Instant.now())) {
            verificationTokenService.revokeToken(vt);
            throw new InvalidTokenException(VerificationErrorMessages.TOKEN_EXPIRED);
        }

        if (vt.isRevoked()) {
            throw new InvalidTokenException(VerificationErrorMessages.TOKEN_REVOKED);
        }

        if (vt.getPurpose() != VerificationTokenPurpose.PASSWORD_RESET) {
            throw new InvalidTokenException(VerificationErrorMessages.INVALID_TOKEN_PURPOSE_PASSWORD_RESET);
        }

        User user = vt.getUser();

        // Check if the new password is the same as the current password
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new SamePasswordException(VerificationErrorMessages.SAME_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.update(user);

        verificationTokenService.revokeToken(vt);
    }
}
