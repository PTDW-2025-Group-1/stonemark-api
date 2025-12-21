package pt.estga.verification.services;

import pt.estga.user.entities.User;
import pt.estga.verification.entities.ActionCode;

import java.util.Optional;

public interface ChatbotVerificationService {

    ActionCode generateTelegramVerificationCode(User user);

    Optional<User> verifyTelegramCode(String code, String telegramId);

}
