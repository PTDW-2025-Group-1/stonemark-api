package pt.estga.content.dtos;

import pt.estga.user.entities.User;

import java.time.Instant;

public record MonumentDetailedResponseDto(
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
        User createdBy,
        User lastModifiedBy
) {
}
