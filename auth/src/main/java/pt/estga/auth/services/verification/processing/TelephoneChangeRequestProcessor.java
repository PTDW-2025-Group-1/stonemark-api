package pt.estga.auth.services.verification.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.auth.entities.request.TelephoneChangeRequest;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.repositories.TelephoneChangeRequestRepository;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.sms.SmsVerificationService;
import pt.estga.shared.exceptions.InvalidTokenException;
import pt.estga.user.entities.User;
import pt.estga.user.repositories.UserRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TelephoneChangeRequestProcessor implements VerificationProcessor {

    private final TelephoneChangeRequestRepository repository;
    private final VerificationTokenService tokenService;
    private final SmsVerificationService telephoneService;
    private final UserRepository userRepository;

    @Override
    public Optional<String> process(VerificationToken token) {
        TelephoneChangeRequest request = repository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Telephone change request not found."));

        User user = userRepository.findById(request.getUser().getId())
                .orElseThrow(() -> new InvalidTokenException("User not found."));
        String newTelephone = request.getNewTelephone();

        // Create a new token for the confirmation of the telephone change
        VerificationToken confirmationToken = tokenService.createAndSaveToken(user, VerificationTokenPurpose.TELEPHONE_CHANGE_CONFIRM);
        request.setVerificationToken(confirmationToken);
        repository.save(request);

        telephoneService.sendVerificationSms(newTelephone, confirmationToken);

        // Revoke the token as it has served its purpose
        tokenService.revokeToken(token);

        return Optional.empty();
    }

    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.TELEPHONE_CHANGE_REQUEST;
    }
}
