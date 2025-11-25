package pt.estga.user.dtos;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequestDto(
        @NotBlank String oldPassword,
        @NotBlank String newPassword
) {
}
