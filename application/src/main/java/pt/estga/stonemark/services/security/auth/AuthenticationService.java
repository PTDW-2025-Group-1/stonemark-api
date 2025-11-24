package pt.estga.stonemark.services.security.auth;

import jakarta.validation.Valid;
import pt.estga.stonemark.dtos.auth.*;
import pt.estga.stonemark.entities.User;

import java.util.Optional;

public interface AuthenticationService {

    Optional<AuthenticationResponseDto> register(@Valid User user);

    Optional<AuthenticationResponseDto> authenticate(String email, String password);

    Optional<AuthenticationResponseDto> refreshToken(String refreshToken);

    Optional<AuthenticationResponseDto> authenticateWithGoogle(String googleToken);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    void logoutFromAllDevices(User user);

    void logoutFromAllOtherDevices(User user, String currentToken);

}
