package pt.estga.stonemark.dtos.user;

import lombok.*;

import java.time.Instant;

@Builder
public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String telephone,
        String role,
        Instant createdAt,
        boolean accountLocked,
        boolean enabled
) { }
