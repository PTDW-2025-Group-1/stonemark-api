package pt.estga.auth.dtos;

import jakarta.validation.constraints.NotNull;
import pt.estga.user.enums.TfaMethod;

public record SetTfaMethodRequestDto(
        @NotNull
        TfaMethod tfaMethod
) { }
