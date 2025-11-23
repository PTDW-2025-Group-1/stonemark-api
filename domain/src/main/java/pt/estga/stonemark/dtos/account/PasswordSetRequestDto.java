package pt.estga.stonemark.dtos.account;

import jakarta.validation.constraints.NotBlank;

public record PasswordSetRequestDto(@NotBlank String newPassword) {
}
