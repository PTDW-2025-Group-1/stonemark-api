package pt.estga.chatbot.telegram.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.estga.chatbot.services.AuthService;
import pt.estga.user.enums.Provider;
import pt.estga.user.services.UserIdentityService;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthService implements AuthService {

    private final UserIdentityService userIdentityService;

    @Override
    public boolean isAuthenticated(String platformUserId) {
        return userIdentityService.findByProviderAndValue(Provider.TELEGRAM, platformUserId).isPresent();
    }

    @Override
    public boolean supports(String platform) {
        return "TELEGRAM".equalsIgnoreCase(platform);
    }
}
