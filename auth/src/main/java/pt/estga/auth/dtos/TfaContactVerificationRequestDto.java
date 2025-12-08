package pt.estga.auth.dtos;

import jakarta.validation.constraints.NotBlank;

public record TfaContactVerificationRequestDto(
        @NotBlank
        String code
) { }
