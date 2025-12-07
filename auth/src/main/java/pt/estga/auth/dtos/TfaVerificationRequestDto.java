package pt.estga.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public record TfaVerificationRequestDto(
        @NotBlank
        String code
) { }
