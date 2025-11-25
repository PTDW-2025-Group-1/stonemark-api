package pt.estga.shared.dtos.auth;

import lombok.*;

@Builder
public record AuthenticationResponseDto(
        String accessToken,
        String refreshToken
) { }
