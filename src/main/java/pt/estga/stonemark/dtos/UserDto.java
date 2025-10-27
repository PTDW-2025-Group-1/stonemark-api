package pt.estga.stonemark.dtos;

import lombok.*;
import pt.estga.stonemark.enums.Role;

import java.time.Instant;

@Builder
public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String telephone,
        Role role,
        Instant createdAt
) { }
