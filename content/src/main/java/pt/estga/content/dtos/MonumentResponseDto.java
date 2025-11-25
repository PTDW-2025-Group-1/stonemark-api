package pt.estga.content.dtos;

import java.time.Instant;

public record MonumentResponseDto(
        Long id,
        String name,
        String description,
        String protectionTitle,
        String website,
        Double latitude,
        Double longitude,
        String address,
        String city,
        Instant createdAt,
        Instant lastModifiedAt,
        String createdBy,
        String lastModifiedBy
) {
}
