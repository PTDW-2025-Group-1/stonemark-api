package pt.estga.auth.dtos;

import lombok.*;

@Builder
public record RegisterRequestDto(
        String firstName,
        String lastName,
        String email,
        String telephone,
        String password,
        String role
) { }
