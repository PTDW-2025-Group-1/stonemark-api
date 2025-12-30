package pt.estga.user.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequestDto(
        @NotBlank(message = "Current password is required")
        @Size(min = 8, max = 100, message = "Current password must be between 8 and 100 characters")
        String oldPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
        String newPassword
) {}
