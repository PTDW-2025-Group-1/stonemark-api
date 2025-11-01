package pt.estga.stonemark.dtos.auth;

import lombok.Builder;

@Builder
public record PasswordResetRequestDto(
        String email
) { }
