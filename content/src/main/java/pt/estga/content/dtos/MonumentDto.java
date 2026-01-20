package pt.estga.content.dtos;

import pt.estga.territory.dtos.AdministrativeDivisionDto;

import java.time.Instant;

public record MonumentDto(
        Long id,
        String name,
        String description,
        Double latitude,
        Double longitude,
        String street,
        String houseNumber,
        AdministrativeDivisionDto parish,
        AdministrativeDivisionDto municipality,
        AdministrativeDivisionDto district,
        Long coverId,
        String protectionTitle,
        String website,
        Long parishId,
        Long municipalityId,
        Long districtId,
        Boolean active,
        Instant createdAt,
        Instant lastModifiedAt
) { }
