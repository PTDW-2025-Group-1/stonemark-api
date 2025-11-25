package pt.estga.shared.dtos.auth;

import lombok.Builder;

@Builder
public record RefreshTokenRequestDto(String refreshToken) {
}
