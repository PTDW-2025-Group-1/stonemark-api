package pt.estga.auth.dtos;

import lombok.*;

@Builder
public record AuthenticationRequestDto(
        String username,
        String password,
        String tfaCode
) { }
