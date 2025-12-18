package pt.estga.chatbots.core.features.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.estga.chatbots.telegram.services.TelegramAuthService;
import pt.estga.user.services.UserContactService;
import pt.estga.user.services.UserIdentityService;

@Configuration
public class AuthConfig {

    @Bean
    public AuthService telegramAuthService(UserContactService userContactService, UserIdentityService userIdentityService) {
        return new TelegramAuthService(userContactService, userIdentityService);
    }
}
