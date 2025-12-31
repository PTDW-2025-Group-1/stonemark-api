package pt.estga.chatbot.whatsapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.chatbot.models.Platform;
import pt.estga.chatbot.services.AuthService;
import pt.estga.user.enums.Provider;
import pt.estga.user.services.UserIdentityService;

@Service
@RequiredArgsConstructor
public class WhatsAppAuthService implements AuthService {

    private final UserIdentityService userIdentityService;

    @Override
    public boolean isAuthenticated(String platformUserId) {
        return userIdentityService.findByProviderAndValue(Provider.WHATSAPP, platformUserId).isPresent();
    }

    @Override
    public boolean supports(Platform platform) {
        return Platform.WHATSAPP.equals(platform);
    }
}
