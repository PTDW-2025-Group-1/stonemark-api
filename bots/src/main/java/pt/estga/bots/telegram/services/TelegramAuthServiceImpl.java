package pt.estga.bots.telegram.services;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.estga.user.entities.User;
import pt.estga.user.repositories.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthServiceImpl implements TelegramAuthService {

    private final UserRepository userRepository;
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    @Override
    public Optional<User> authenticateUser(String chatId, String phoneNumber) {
        log.info("Received phone number: {}", phoneNumber);
        String sanitized = sanitizePhone(phoneNumber);
        log.info("Sanitized phone number: {}", sanitized);

        Optional<User> userOptional = userRepository.findByTelephone(sanitized);
        userOptional.ifPresent(user -> {
            user.setTelegramChatId(chatId);
            userRepository.save(user);
        });

        return userOptional;
    }

    private String sanitizePhone(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        try {
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(input, "PT");
            return phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            log.error("Could not parse phone number: {}", input, e);
            return input.replaceAll("\\D", "");
        }
    }
}
