package pt.estga.proposals.dtos;

import jakarta.validation.constraints.NotNull;

public record SelectExistingMarkRequestDto(
        @NotNull(message = "Existing mark ID must be provided.")
        Long existingMarkId
) {
}
