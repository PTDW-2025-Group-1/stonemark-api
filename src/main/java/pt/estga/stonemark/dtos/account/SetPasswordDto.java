package pt.estga.stonemark.dtos.account;

import jakarta.validation.constraints.NotBlank;

public record SetPasswordDto(@NotBlank String newPassword) {
}
