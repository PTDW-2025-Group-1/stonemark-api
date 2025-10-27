package pt.estga.stonemark.dtos;

import lombok.*;

@Builder
public record ChangePasswordRequestDto(
        String currentPassword,
        String newPassword,
        String confirmationPassword
) { }