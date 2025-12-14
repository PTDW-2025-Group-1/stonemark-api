package pt.estga.user.dtos;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Builder
public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String username,
        List<UserContactDto> contacts,
        String role,
        Instant createdAt,
        boolean accountLocked,
        boolean enabled
) { }
