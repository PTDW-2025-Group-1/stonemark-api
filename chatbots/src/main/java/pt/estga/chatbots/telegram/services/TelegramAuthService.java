package pt.estga.chatbots.telegram.services;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pt.estga.chatbots.core.features.auth.AuthService;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.Provider;
import pt.estga.user.services.UserContactService;
import pt.estga.user.services.UserIdentityService;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class TelegramAuthService implements AuthService {

    private final UserContactService userContactService;
    private final UserIdentityService userIdentityService;

    @Override
    public Optional<User> authenticate(String platformUserId, String authenticationData) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(authenticationData, "PT");
            String internationalFormat = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
            log.info("Attempting to authenticate user with international phone number: {}", internationalFormat);

            Optional<User> userOptional = userContactService.findByValue(internationalFormat)
                    .map(UserContact::getUser);

            userOptional.ifPresent(user -> {
                userIdentityService.createAndAssociate(user, Provider.TELEGRAM, platformUserId);
                log.info("User {} authenticated and Telegram chat ID updated.", user.getUsername());
            });

            return userOptional;

        } catch (NumberParseException e) {
            log.warn("Could not parse phone number {}: {}", authenticationData, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean supports(String platform) {
        return "TELEGRAM".equalsIgnoreCase(platform);
    }
}
