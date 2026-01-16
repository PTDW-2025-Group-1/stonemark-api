package pt.estga.content.dtos;

import pt.estga.administrative.dto.AdministrativeDivisionDto;

public record MonumentMapDto(
        Long id,
        String name,
        Double latitude,
        Double longitude,
        String protectionTitle,
        String website,
        AdministrativeDivisionDto parish
) {
}
