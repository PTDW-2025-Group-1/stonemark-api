package pt.estga.stonemark.dtos.auth;

import jakarta.validation.constraints.NotBlank;

public record SetPasswordRequestDto(
        @NotBlank String password,
        @NotBlank String confirmationPassword
) { }
