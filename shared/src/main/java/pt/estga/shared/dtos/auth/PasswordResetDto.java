package pt.estga.shared.dtos.auth;

import lombok.Builder;

@Builder
public record PasswordResetDto(
    String token,
    String newPassword
) { }
