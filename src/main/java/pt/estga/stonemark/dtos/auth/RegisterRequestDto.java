package pt.estga.stonemark.dtos.auth;

import lombok.*;
import pt.estga.stonemark.enums.Role;

@Builder
public record RegisterRequestDto(
        String firstName,
        String lastName,
        String email,
        String telephone,
        String password,
        Role role
) { }
