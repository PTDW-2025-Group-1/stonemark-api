package pt.estga.stonemark.dtos.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthenticationRequestDto(@NotBlank String token) {
}
