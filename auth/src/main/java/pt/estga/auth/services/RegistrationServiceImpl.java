package pt.estga.auth.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.shared.exceptions.EmailAlreadyTakenException;
import pt.estga.user.Role;
import pt.estga.user.entities.User;
import pt.estga.user.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserService userService;
    private final KeycloakAdminService keycloakAdminService;

    @Value("${application.security.email-verification-required:false}")
    private boolean emailVerificationRequired;

    @Override
    @Transactional
    public void register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        var email = user.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be null or blank");
        }
        if (userService.existsByEmail(email)) {
            throw new EmailAlreadyTakenException("email already in use");
        }

        user.setEnabled(!emailVerificationRequired);
        
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        String keycloakId = keycloakAdminService.createUserInKeycloak(
                user.getEmail(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                !emailVerificationRequired
        );

        user.setKeycloakId(keycloakId);
        userService.update(user);
    }
}
