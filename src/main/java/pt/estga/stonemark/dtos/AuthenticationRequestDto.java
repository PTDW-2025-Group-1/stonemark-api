package pt.estga.stonemark.dtos;

public record AuthenticationRequestDto(
        String email,
        String password
) {}
