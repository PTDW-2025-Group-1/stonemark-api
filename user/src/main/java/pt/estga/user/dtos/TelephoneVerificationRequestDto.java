package pt.estga.user.dtos;

import jakarta.validation.constraints.NotBlank;

public record TelephoneVerificationRequestDto(
        @NotBlank String telephone
) {
}
