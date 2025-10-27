package pt.estga.stonemark.dtos;

import lombok.*;

@Builder
public record AuthenticationRequestDto(
        String email,
        String password
) { }
