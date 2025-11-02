package pt.estga.stonemark.services.security.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.exceptions.InvalidTokenException;
import pt.estga.stonemark.services.security.token.VerificationTokenService;
import pt.estga.stonemark.services.security.verification.processing.VerificationProcessor;
import pt.estga.stonemark.services.security.verification.processing.VerificationProcessorFactory;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class VerificationProcessingServiceImpl implements VerificationProcessingService {

    private final VerificationTokenService verificationTokenService;
    private final VerificationProcessorFactory verificationProcessorFactory;

    @Transactional
    @Override
    public void processTokenConfirmation(String token) {
        VerificationToken vt = verificationTokenService.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token not found."));

        if (vt.getExpiresAt().isBefore(Instant.now())) {
            verificationTokenService.revokeToken(token);
            throw new InvalidTokenException("Token has expired.");
        }

        VerificationProcessor processor = verificationProcessorFactory.getProcessor(vt.getPurpose());
        if (processor == null) {
            throw new IllegalStateException("Token has an unknown purpose: " + vt.getPurpose());
        }

        processor.process(vt);

        verificationTokenService.revokeToken(token);
    }
}
