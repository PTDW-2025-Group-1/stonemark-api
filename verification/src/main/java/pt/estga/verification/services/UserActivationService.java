package pt.estga.verification.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.verification.entities.ActionCode;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserService;

import java.util.Optional;

/**
 * Service responsible for activating a user and consuming the associated action code.
 * This is typically used after successful email or telephone verification.
 */
@Service
@RequiredArgsConstructor
public class UserActivationService {

    private final UserService userService;
    private final ActionCodeService actionCodeService;

    /**
     * Activates the user associated with the given action code and consumes the code.
     * The user's account will only be set to enabled if it's not already.
     *
     * @param code The {@link ActionCode} whose associated user needs to be activated.
     * @return An empty Optional, as this operation typically doesn't return a specific string value.
     */
    public Optional<String> activateUserAndConsumeCode(ActionCode code) {
        User user = code.getUser();

        // Only enable the user if they are not already enabled.
        // This decouples the user's overall activation from individual contact verifications,
        // ensuring the user is activated once, while still allowing contact codes to be consumed.
        if (!user.isEnabled()) {
            user.setEnabled(true);
            userService.update(user);
        }

        actionCodeService.consumeCode(code);
        return Optional.empty();
    }
}
