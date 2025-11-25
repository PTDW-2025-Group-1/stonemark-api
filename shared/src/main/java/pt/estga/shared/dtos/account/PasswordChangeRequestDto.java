package pt.estga.shared.dtos.account;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequestDto(
        @NotBlank String oldPassword,
        @NotBlank String newPassword
) {
}
