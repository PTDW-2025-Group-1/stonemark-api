package pt.estga.stonemark.dtos;

import pt.estga.stonemark.enums.Role;

public record RegisterRequestDto(
        String firstName,
        String lastName,
        String email,
        String telephone,
        String password,
        Role role
) {}
