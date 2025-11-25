package pt.estga.auth.dtos;

import lombok.Builder;

@Builder
public record RefreshTokenRequestDto(String refreshToken) {
}
