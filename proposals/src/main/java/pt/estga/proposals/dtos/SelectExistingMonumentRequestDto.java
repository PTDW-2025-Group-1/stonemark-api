package pt.estga.proposals.dtos;

import jakarta.validation.constraints.NotNull;

public record SelectExistingMonumentRequestDto(
        @NotNull(message = "Existing monument ID must be provided.")
        Long existingMonumentId
) {
}
