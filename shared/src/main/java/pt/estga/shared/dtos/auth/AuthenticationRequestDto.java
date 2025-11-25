package pt.estga.shared.dtos.auth;

import lombok.*;

@Builder
public record AuthenticationRequestDto(
        String email,
        String password
) { }
