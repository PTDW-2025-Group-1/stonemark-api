package pt.estga.auth.dtos;

import lombok.*;

@Builder
public record AuthenticationResponseDto(
        String accessToken,
        String refreshToken,
        String role,
        boolean tfaEnabled,
        boolean tfaRequired
) { }
