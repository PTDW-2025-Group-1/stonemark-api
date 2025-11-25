package pt.estga.shared.dtos.account;

import jakarta.validation.constraints.NotBlank;

public record PasswordSetRequestDto(@NotBlank String newPassword) {
}
