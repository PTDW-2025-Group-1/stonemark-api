package pt.estga.stonemark.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
public record AuthenticationResponseDto(
        String accessToken,
        String refreshToken
) { }
