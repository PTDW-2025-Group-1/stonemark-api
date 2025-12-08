package pt.estga.auth.services.passwordreset;

import pt.estga.user.entities.User;
import pt.estga.user.enums.ContactType;

import java.util.Optional;

public interface PasswordResetService {

    void initiatePasswordReset(String contactValue);

    Optional<User> validatePasswordResetToken(String token);

    void resetPassword(String token, String newPassword);

}
