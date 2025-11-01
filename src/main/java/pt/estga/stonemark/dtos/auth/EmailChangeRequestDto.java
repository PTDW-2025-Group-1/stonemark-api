package pt.estga.stonemark.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailChangeRequestDto(
        @NotBlank(message = "Email cannot be blank.")
        @Email(message = "Invalid email format.")
        String newEmail
) {}
