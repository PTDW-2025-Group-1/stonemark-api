package pt.estga.stonemark.dtos.auth;

import lombok.*;

@Builder
public record AuthenticationResponseDto(
        String accessToken,
        String refreshToken
) { }
