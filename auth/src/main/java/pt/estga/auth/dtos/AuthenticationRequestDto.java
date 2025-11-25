package pt.estga.auth.dtos;

import lombok.*;

@Builder
public record AuthenticationRequestDto(
        String email,
        String password
) { }
