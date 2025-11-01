package pt.estga.stonemark.services.auth;

import pt.estga.stonemark.dtos.auth.AuthenticationRequestDto;
import pt.estga.stonemark.dtos.auth.AuthenticationResponseDto;
import pt.estga.stonemark.dtos.auth.RegisterRequestDto;

import java.util.Optional;

public interface AuthenticationService {

    Optional<AuthenticationResponseDto> register(RegisterRequestDto request);

    Optional<AuthenticationResponseDto> authenticate(AuthenticationRequestDto request);

    Optional<AuthenticationResponseDto> refreshToken(String refreshToken);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

}
