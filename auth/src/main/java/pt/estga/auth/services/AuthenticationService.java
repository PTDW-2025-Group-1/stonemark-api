package pt.estga.auth.services;

import pt.estga.auth.dtos.AuthenticationResponseDto;
import pt.estga.user.entities.User;

import java.util.Optional;

public interface AuthenticationService {

    Optional<AuthenticationResponseDto> register(User user);

    Optional<AuthenticationResponseDto> authenticate(String username, String password, String tfaCode);

    Optional<AuthenticationResponseDto> refreshToken(String refreshToken);

    void logoutFromAllDevices(User user);

}
