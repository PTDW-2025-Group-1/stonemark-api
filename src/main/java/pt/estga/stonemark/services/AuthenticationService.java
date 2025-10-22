package pt.estga.stonemark.services;

import pt.estga.stonemark.dtos.AuthenticationRequestDto;
import pt.estga.stonemark.dtos.AuthenticationResponseDto;
import pt.estga.stonemark.dtos.RegisterRequestDto;

public interface AuthenticationService {

    AuthenticationResponseDto register(RegisterRequestDto request);

    AuthenticationResponseDto authenticate(AuthenticationRequestDto request);

}
