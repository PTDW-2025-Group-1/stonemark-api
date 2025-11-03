package pt.estga.stonemark.services.security.verification;

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
import pt.estga.stonemark.services.UserService;
import pt.estga.stonemark.services.security.token.VerificationTokenService;
import pt.estga.stonemark.services.security.verification.processing.VerificationProcessor;
import pt.estga.stonemark.services.security.verification.processing.VerificationProcessorFactory;

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
                    .orElseThrow(() -> new InvalidTokenException("Token not found."));

            if (vt.getExpiresAt().isBefore(Instant.now())) {
                verificationTokenService.revokeToken(vt);
                throw new InvalidTokenException("Token has expired.");
            }

            VerificationProcessor processor = verificationProcessorFactory.getProcessor(vt.getPurpose());
            Optional<String> resultToken = processor.process(vt);

            if (resultToken.isPresent()) {
                return ConfirmationResponseDto.passwordResetRequired(resultToken.get());
            } else {
                return ConfirmationResponseDto.success("Confirmation successful.");
            }
        } catch (InvalidTokenException e) {
            return ConfirmationResponseDto.error(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ConfirmationResponseDto.error("Invalid token purpose: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public ConfirmationResponseDto processCodeConfirmation(String code) {
        try {
            VerificationToken vt = verificationTokenService.findByCode(code)
                    .orElseThrow(() -> new InvalidTokenException("Code not found."));

            if (vt.getExpiresAt().isBefore(Instant.now())) {
                verificationTokenService.revokeToken(vt);
                throw new InvalidTokenException("Code has expired.");
            }

            VerificationProcessor processor = verificationProcessorFactory.getProcessor(vt.getPurpose());
            Optional<String> resultToken = processor.process(vt);

            if (resultToken.isPresent()) {
                return ConfirmationResponseDto.passwordResetRequired(resultToken.get());
            } else {
                return ConfirmationResponseDto.success("Confirmation successful.");
            }
        } catch (InvalidTokenException e) {
            return ConfirmationResponseDto.error(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ConfirmationResponseDto.error("Invalid code purpose: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void processPasswordReset(String token, String newPassword) {
        VerificationToken vt = verificationTokenService.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token not found."));

        if (vt.getExpiresAt().isBefore(Instant.now())) {
            verificationTokenService.revokeToken(vt);
            throw new InvalidTokenException("Token has expired.");
        }

        if (vt.getPurpose() != VerificationTokenPurpose.PASSWORD_RESET) {
            throw new InvalidTokenException("Invalid token purpose for password reset.");
        }

        User user = vt.getUser();

        // Check if the new password is the same as the current password
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new SamePasswordException("New password cannot be the same as the old password.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.update(user);

        verificationTokenService.revokeToken(vt);
    }
}
