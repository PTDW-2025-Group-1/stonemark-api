package pt.estga.user.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.shared.exceptions.InvalidGoogleTokenException;
import pt.estga.user.dtos.LinkedProviderDto;
import pt.estga.user.entities.User;
import pt.estga.user.enums.Provider;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
@AllArgsConstructor
public class AccountSocialService {

    private final UserService userService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserIdentityService userIdentityService;
    private final TelegramAccountLinkerService telegramAccountLinkerService;

    @Transactional
    public void unlinkSocialAccount(User user, Provider provider) {

        User managedUser = userService
                .findByIdWithIdentities(user.getId())
                .orElseThrow();

        boolean hasPassword =
                managedUser.getPassword() != null &&
                        !managedUser.getPassword().isBlank();

        boolean isLastProvider = managedUser.getIdentities().size() <= 1;

        if (!hasPassword && isLastProvider) {
            throw new IllegalStateException(
                    "You must set a password before disconnecting the last authentication provider."
            );
        }

        boolean removed = managedUser.getIdentities()
                .removeIf(identity -> identity.getProvider() == provider);

        if (!removed) {
            throw new IllegalStateException("Provider not linked to user.");
        }

    }

    public List<LinkedProviderDto> getLinkedProviders(User user) {
        User managedUser = userService
                .findByIdWithIdentities(user.getId())
                .orElseThrow();

        return managedUser.getIdentities().stream()
                .map(identity -> new LinkedProviderDto(identity.getProvider()))
                .toList();
    }

    public void linkGoogleAccount(User user, String token) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(token);
            if (idToken == null) {
                throw new InvalidGoogleTokenException("Invalid Google token.");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleId = payload.getSubject();

            userIdentityService.createAndAssociate(user, Provider.GOOGLE, googleId);

        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidGoogleTokenException("Error while verifying Google token.");
        }
    }

    public void linkTelegramAccount(User user, String token) {
        telegramAccountLinkerService.linkAccount(user, token);
    }
}
