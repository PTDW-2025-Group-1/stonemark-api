package pt.estga.chatbots.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class AuthServiceFactory {

    private final List<AuthService> authServices;

    public AuthService getAuthService(String platform) {
        return authServices.stream()
                .filter(service -> service.supports(platform))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No AuthService found for platform: " + platform));
    }
}
