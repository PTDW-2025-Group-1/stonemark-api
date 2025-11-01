package pt.estga.stonemark.dtos.auth;

import lombok.*;

@Builder
public record ChangePasswordRequestDto(
        String currentPassword,
        String newPassword,
        String confirmationPassword
) { }