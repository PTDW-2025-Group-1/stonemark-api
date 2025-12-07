package pt.estga.bots.telegram.services;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.estga.user.entities.User;
import pt.estga.user.service.UserService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthServiceImpl implements TelegramAuthService {

    private final UserService userService;

    @Override
    public Optional<User> authenticateUser(String telegramChatId, String phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumber, "PT");
            String internationalFormat = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
            log.info("Attempting to authenticate user with international phone number: {}", internationalFormat);

            Optional<User> userOptional = userService.findByTelephone(internationalFormat);

            userOptional.ifPresent(user ->
                    log.info("User {} authenticated and Telegram chat ID updated.", user.getUsername())
            );

            return userOptional;

        } catch (NumberParseException e) {
            log.warn("Could not parse phone number {}: {}", phoneNumber, e.getMessage());
            return Optional.empty();
        }
    }
}
