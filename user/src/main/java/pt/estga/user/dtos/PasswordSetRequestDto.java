package pt.estga.user.dtos;

import jakarta.validation.constraints.NotBlank;

public record PasswordSetRequestDto(@NotBlank String newPassword) {
}
