package pt.estga.auth.dtos;

import lombok.Builder;

@Builder
public record TfaSetupResponseDto(
        String secret,
        String qrCodeImageUrl
) { }
