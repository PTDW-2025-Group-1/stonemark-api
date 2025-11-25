package pt.estga.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthenticationRequestDto(@NotBlank String token) {
}
