package pt.estga.proposal.dtos;

import jakarta.validation.constraints.NotNull;

public record SelectExistingMonumentRequestDto(
        @NotNull(message = "Existing monument ID must be provided.")
        Long existingMonumentId
) {
}
