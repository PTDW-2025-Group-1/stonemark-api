package pt.estga.bots.telegram.services;

import pt.estga.user.entities.User;

import java.util.Optional;

public interface TelegramAuthService {
    Optional<User> authenticateUser(String telegramChatId, String phoneNumber);
}
