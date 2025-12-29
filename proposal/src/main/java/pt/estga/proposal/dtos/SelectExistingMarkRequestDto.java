package pt.estga.proposal.dtos;

import jakarta.validation.constraints.NotNull;

public record SelectExistingMarkRequestDto(
        @NotNull(message = "Existing mark ID must be provided.")
        Long existingMarkId
) {
}
