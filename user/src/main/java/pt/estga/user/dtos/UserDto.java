package pt.estga.user.dtos;

import lombok.*;
import pt.estga.shared.enums.UserRole;
import pt.estga.user.enums.TfaMethod;

import java.time.Instant;

@Builder
public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String username,
        UserRole role,
        Instant createdAt,
        TfaMethod tfaMethod,
        boolean accountLocked,
        boolean enabled
) { }
