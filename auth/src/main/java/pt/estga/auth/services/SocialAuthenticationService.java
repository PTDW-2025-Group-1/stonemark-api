package pt.estga.auth.services;

import pt.estga.auth.dtos.AuthenticationResponseDto;

import java.util.Optional;

public interface SocialAuthenticationService {

    Optional<AuthenticationResponseDto> authenticateWithGoogle(String googleToken);

    Optional<AuthenticationResponseDto> authenticateWithTelegram(String telegramData);

}
