package pt.estga.user.dtos;

import jakarta.validation.constraints.NotBlank;

public record TelephoneChangeRequestDto(
        @NotBlank String newTelephone
) {
}
