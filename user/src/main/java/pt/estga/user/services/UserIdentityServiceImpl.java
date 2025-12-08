package pt.estga.user.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserIdentity;
import pt.estga.user.enums.Provider;
import pt.estga.user.repositories.UserIdentityRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserIdentityServiceImpl implements UserIdentityService {

    private final UserIdentityRepository userIdentityRepository;
    private final UserService userService; // Inject UserService to update the User

    @Override
    public Optional<UserIdentity> findByProviderAndIdentity(Provider provider, String identityValue) {
        return userIdentityRepository.findByProviderAndIdentity(provider, identityValue);
    }

    @Override
    @Transactional
    public UserIdentity createAndAssociateUserIdentity(User user, Provider provider, String identityValue) {
        UserIdentity identity = UserIdentity.builder()
                .provider(provider)
                .identity(identityValue)
                .user(user)
                .build();

        // Add the new identity to the user's collection
        // Ensure the list is mutable before adding
        if (user.getIdentities() == null) {
            user.setIdentities(new java.util.ArrayList<>());
        }
        user.getIdentities().add(identity);

        // Save the identity directly, or rely on cascade from user update.
        // Given the UserIdentityRepository is available, explicit save is clearer.
        userIdentityRepository.save(identity);

        // Update the user to ensure the relationship is managed by JPA
        // This might trigger a save on the identity due to cascade, but explicit save above is fine.
        userService.update(user);

        return identity;
    }
}
