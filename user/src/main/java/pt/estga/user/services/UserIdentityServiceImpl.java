package pt.estga.user.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserIdentity;
import pt.estga.user.enums.Provider;
import pt.estga.user.repositories.UserIdentityRepository;
import pt.estga.user.repositories.UserRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserIdentityServiceImpl implements UserIdentityService {

    private final UserIdentityRepository userIdentityRepository;
    private final UserRepository userRepository;

    @Override
    public Optional<UserIdentity> findByProviderAndValue(Provider provider, String value) {
        return userIdentityRepository.findByProviderAndValue(provider, value);
    }

    @Override
    public Optional<UserIdentity> findByUserAndProvider(User user, Provider provider) {
        return userIdentityRepository.findByUserAndProvider(user, provider);
    }

    @Override
    @Transactional
    public UserIdentity createAndAssociate(User user, Provider provider, String identityValue) {
        // Verify that the user doesn't have an identity with the given provider yet
        boolean identityExists = userIdentityRepository.findByUserAndProvider(user, provider).isPresent();

        if (identityExists) {
            throw new IllegalStateException("User already has an identity with provider " + provider);
        }

        UserIdentity identity = UserIdentity.builder()
                .provider(provider)
                .value(identityValue)
                .user(user)
                .build();

        return userIdentityRepository.save(identity);
    }

    @Override
    @Transactional
    public UserIdentity createOrUpdateTelegramIdentity(User user, String telegramId) {
        // Re-fetch the user to ensure it's attached to the current session
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found with ID: " + user.getId()));

        Optional<UserIdentity> existingIdentity = userIdentityRepository.findByUserAndProvider(managedUser, Provider.TELEGRAM);

        if (existingIdentity.isPresent()) {
            log.info("Updating Telegram identity for user: {}", managedUser.getId());
            existingIdentity.get().setValue(telegramId);
            return userIdentityRepository.save(existingIdentity.get());
        } else {
            log.info("Creating Telegram identity for user: {}", managedUser.getId());
            return createAndAssociate(managedUser, Provider.TELEGRAM, telegramId);
        }
    }

    @Override
    public void delete(UserIdentity userIdentity) {
        userIdentityRepository.delete(userIdentity);
    }

    @Override
    public void deleteByUserAndProvider(User user, Provider provider) {
        userIdentityRepository.deleteByUserAndProvider(user, provider);
    }
}
