package pt.estga.auth.services;

import jakarta.validation.Valid;
import pt.estga.auth.dtos.AuthenticationResponseDto;
import pt.estga.user.entities.User;

import java.util.Optional;

public interface AuthenticationService {

    Optional<AuthenticationResponseDto> register(@Valid User user);

    Optional<AuthenticationResponseDto> authenticate(String email, String password, String tfaCode);

    Optional<AuthenticationResponseDto> refreshToken(String refreshToken);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    void logoutFromAllDevices(User user);

}
