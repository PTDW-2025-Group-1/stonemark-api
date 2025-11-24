package pt.estga.stonemark.dtos.auth;

import lombok.*;

@Builder
public record AuthenticationRequestDto(
        String email,
        String password
) { }
