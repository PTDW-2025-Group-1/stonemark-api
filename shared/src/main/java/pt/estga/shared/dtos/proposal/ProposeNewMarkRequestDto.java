package pt.estga.shared.dtos.proposal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProposeNewMarkRequestDto {
    @NotBlank(message = "New mark name must be provided.")
    private String name;
    private String description;
}
