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
        Long coverId,
        Instant createdAt,
        Instant lastModifiedAt
) { }
