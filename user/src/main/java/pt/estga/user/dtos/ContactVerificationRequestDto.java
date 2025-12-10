package pt.estga.user.dtos;

import jakarta.validation.constraints.NotBlank;
import pt.estga.user.enums.ContactType;

public record ContactVerificationRequestDto(
        @NotBlank String value,
        ContactType type
) {
}
