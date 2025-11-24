package pt.estga.stonemark.dtos.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailChangeRequestDto(
        @NotBlank @Email String newEmail
) {
}
