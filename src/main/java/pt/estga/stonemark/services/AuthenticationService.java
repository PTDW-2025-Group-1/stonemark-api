package pt.estga.stonemark.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pt.estga.stonemark.dtos.AuthenticationRequestDto;
import pt.estga.stonemark.dtos.AuthenticationResponseDto;
import pt.estga.stonemark.dtos.RegisterRequestDto;

import java.io.IOException;

public interface AuthenticationService {

    AuthenticationResponseDto register(RegisterRequestDto request);

    AuthenticationResponseDto authenticate(AuthenticationRequestDto request);

    AuthenticationResponseDto refreshToken(String refreshToken);

}
