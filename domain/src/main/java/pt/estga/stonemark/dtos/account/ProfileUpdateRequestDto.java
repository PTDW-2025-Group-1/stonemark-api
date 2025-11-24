package pt.estga.stonemark.dtos.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequestDto(
        @NotBlank() @Size(max = 100) String firstName,
        @NotBlank() @Size(max = 100) String lastName
) {
}
