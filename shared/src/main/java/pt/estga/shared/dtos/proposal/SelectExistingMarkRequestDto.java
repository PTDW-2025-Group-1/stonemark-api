package pt.estga.shared.dtos.proposal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SelectExistingMarkRequestDto {
    @NotNull(message = "Existing mark ID must be provided.")
    private Long existingMarkId;
}
