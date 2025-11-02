package pt.estga.stonemark.services.auth;

import jakarta.validation.Valid;
import pt.estga.stonemark.dtos.auth.*;
import pt.estga.stonemark.entities.User;

import java.security.Principal;
import java.util.Optional;

public interface AuthenticationService {

    Optional<AuthenticationResponseDto> register(@Valid RegisterRequestDto request);

    Optional<AuthenticationResponseDto> authenticate(@Valid AuthenticationRequestDto request);

    Optional<AuthenticationResponseDto> refreshToken(String refreshToken);

    Optional<AuthenticationResponseDto> authenticateWithGoogle(String googleToken);

    void disconnectGoogle(User user);

    void processPasswordChangeRequest(@Valid ChangePasswordRequestDto request, Principal connectedUser);

    void setPassword(@Valid SetPasswordRequestDto request, Principal connectedUser);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

}
