package pt.estga.shared.dtos.proposal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SelectExistingMonumentRequestDto {
    @NotNull(message = "Existing monument ID must be provided.")
    private Long existingMonumentId;
}
