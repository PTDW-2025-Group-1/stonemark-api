package pt.estga.stonemark.services.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.VerificationToken;
import pt.estga.stonemark.repositories.UserRepository;
import pt.estga.stonemark.repositories.VerificationTokenRepository;
import pt.estga.stonemark.services.EmailService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private static final long EXPIRY_HOURS = 24;

    private final VerificationTokenRepository tokenRepo;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${application.base-url:http://localhost:8080}")
    private String backendBaseUrl;

    @Transactional
    @Override
    public String createAndSendToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken vt = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryAt(Instant.now().plus(EXPIRY_HOURS, ChronoUnit.HOURS))
                .build();
        tokenRepo.save(vt);

        String link = backendBaseUrl + "/api/auth/confirm?token=" + token;
        String subject = "Please verify your email";
        String body = "Click to verify your account: " + link;

        emailService.sendEmail(user.getEmail(), subject, body);

        return token;
    }

    @Transactional
    @Override
    public boolean confirmToken(String token) {
        VerificationToken vt = tokenRepo.findByToken(token).orElse(null);
        if (vt == null) return false;
        if (vt.getExpiryAt().isBefore(Instant.now())) {
            tokenRepo.delete(vt);
            return false;
        }

        User user = vt.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepo.delete(vt);
        return true;
    }
}
