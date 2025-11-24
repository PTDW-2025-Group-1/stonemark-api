package pt.estga.stonemark.dtos.auth;

import lombok.Builder;

@Builder
public record PasswordResetDto(
    String token,
    String newPassword
) { }
