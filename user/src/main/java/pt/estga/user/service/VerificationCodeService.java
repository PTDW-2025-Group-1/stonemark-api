package pt.estga.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.user.entities.User;
import pt.estga.user.entities.VerificationCode;
import pt.estga.user.repositories.VerificationCodeRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 15;
    private final VerificationCodeRepository verificationCodeRepository;
    private final SecureRandom random = new SecureRandom();

    public String generateCode(User user, String newTelephone) {
        String code = generateRandomCode();

        verificationCodeRepository.findAllByUserAndUsedFalse(user)
                .forEach(existing -> {
                    existing.setUsed(true);
                    verificationCodeRepository.save(existing);
                });

        VerificationCode verificationCode = VerificationCode.builder()
                .user(user)
                .code(code)
                .newTelephone(newTelephone)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .used(false)
                .build();

        verificationCodeRepository.save(verificationCode);
        return code;
    }

    public boolean validateCode(User user, String newTelephone, String code) {
        Optional<VerificationCode> verificationCodeOpt = verificationCodeRepository
                .findByUserAndNewTelephoneAndCode(user, newTelephone, code);

        if (verificationCodeOpt.isEmpty()) {
            return false;
        }

        VerificationCode verificationCode = verificationCodeOpt.get();

        if (verificationCode.isUsed() || verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);
        return true;
    }

    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
