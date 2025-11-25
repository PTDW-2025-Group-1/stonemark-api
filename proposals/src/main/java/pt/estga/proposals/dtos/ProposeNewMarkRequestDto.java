package pt.estga.proposals.dtos;

import jakarta.validation.constraints.NotBlank;

public record ProposeNewMarkRequestDto(
        @NotBlank(message = "New mark name must be provided.")
        String name,
        String description
) {
}
