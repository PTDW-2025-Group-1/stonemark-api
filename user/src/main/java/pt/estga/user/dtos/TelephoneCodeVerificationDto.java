package pt.estga.user.dtos;

import jakarta.validation.constraints.NotBlank;

public record TelephoneCodeVerificationDto(
        @NotBlank String newTelephone,
        @NotBlank String code
) { }