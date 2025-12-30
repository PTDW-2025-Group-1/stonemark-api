package pt.estga.auth.dtos;

import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Builder
public record RegisterRequestDto(
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must be at most 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must be at most 50 characters")
        String lastName,

        @NotBlank(message = "Username is required")
        @Size(max = 30, message = "Username must be at most 30 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password
) { }
