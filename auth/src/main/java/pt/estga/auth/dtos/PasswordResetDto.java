package pt.estga.auth.dtos;

import lombok.Builder;

@Builder
public record PasswordResetDto(
    String token,
    String newPassword
) { }
